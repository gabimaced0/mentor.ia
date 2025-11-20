#!/bin/bash

# ==============================================================================
# VARIÁVEIS DE CONFIGURAÇÃO (PREENCHA AQUI)
# ==============================================================================
RESOURCE_GROUP="rg-mentor-ia"           # Nome sugerido para o grupo
LOCATION="brazilsouth"                  # Região principal (onde o Web App e ACI ficarão)
LOCATION_SECONDARY="eastus"             # Região do Storage (pode ser diferente)
ACI_DNS_LABEL="rabbitmq-broker-unico"   # Rótulo de DNS para acesso público ao RabbitMQ

# App Service (Web App)
APP_PLAN_NAME="ASP-rgmentoria-a806"
WEB_APP_NAME="appmentoria"
RUNTIME="JAVA|21"                       

# Banco de Dados SQL
SQL_SERVER_NAME="rm558962"              # Nome do servidor (deve ser único globalmente)
SQL_DB_NAME="dbmentor"
SQL_ADMIN_USER="<SEU_USUARIO_ADMIN>"    
SQL_ADMIN_PASS="<SUA_SENHA_FORTE>"      

# Storage Account e RabbitMQ
STORAGE_ACCOUNT_NAME="storageacctmq"    # Deve ser único, minúsculas e números
RABBITMQ_SHARE_NAME="rabbitmq-share"    # Nome do volume persistente (File Share)
ACI_NAME="rabbitmq-aci"
ACI_IMAGE="rabbitmq:3-management"
RABBITMQ_USER="admin"                   # Usuário padrão
RABBITMQ_PASS="admin"                   # Senha padrão (mudar em produção!)

# ==============================================================================
# 1. CRIAÇÃO DO RESOURCE GROUP
# ==============================================================================
echo "Iniciando provisionamento em $LOCATION..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# ==============================================================================
# 2. CRIAÇÃO DO STORAGE ACCOUNT (para persistência do RabbitMQ)
# ==============================================================================
echo "Criando Storage Account ($STORAGE_ACCOUNT_NAME) em $LOCATION_SECONDARY..."
az storage account create \
    --name $STORAGE_ACCOUNT_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION_SECONDARY \
    --sku Standard_LRS \
    --kind StorageV2

# 2.1 OBTÉM A CHAVE DO STORAGE ACCOUNT E CRIA O FILE SHARE
echo "Obtendo chave do Storage Account para persistência do RabbitMQ..."
STORAGE_KEY=$(az storage account keys list \
    --resource-group $RESOURCE_GROUP \
    --account-name $STORAGE_ACCOUNT_NAME \
    --query "[0].value" -o tsv)

echo "Criando File Share (volume persistente) $RABBITMQ_SHARE_NAME..."
az storage share create \
    --account-name $STORAGE_ACCOUNT_NAME \
    --name $RABBITMQ_SHARE_NAME \
    --account-key $STORAGE_KEY

# ==============================================================================
# 3. CRIAÇÃO DO AZURE SQL SERVER E DATABASE
# ==============================================================================
echo "Criando SQL Server ($SQL_SERVER_NAME)..."
az sql server create \
    --name $SQL_SERVER_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user $SQL_ADMIN_USER \
    --admin-password $SQL_ADMIN_PASS

echo "Configurando Firewall do SQL para permitir acesso do Azure..."
az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER_NAME \
    --name AllowAzureServices \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0

echo "Criando Banco de Dados SQL ($SQL_DB_NAME)..."
az sql db create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER_NAME \
    --name $SQL_DB_NAME \
    --service-objective Basic

# ==============================================================================
# 4. CRIAÇÃO DO APP SERVICE PLAN E WEB APP
# ==============================================================================
echo "Criando App Service Plan (Linux B1)..."
az appservice plan create \
    --name $APP_PLAN_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --sku B1 \
    --is-linux

echo "Criando Web App ($WEB_APP_NAME)..."
az webapp create \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --plan $APP_PLAN_NAME \
    --runtime $RUNTIME

# ==============================================================================
# 5. CRIAÇÃO DO APPLICATION INSIGHTS (MONITORAMENTO)
# ==============================================================================
echo "Criando Application Insights..."
az monitor app-insights component create \
    --app $WEB_APP_NAME \
    --location $LOCATION \
    --resource-group $RESOURCE_GROUP \
    --kind web \
    --application-type web

echo "Vinculando App Insights ao Web App..."
INSTRUMENTATION_KEY=$(az monitor app-insights component show --app $WEB_APP_NAME -g $RESOURCE_GROUP --query instrumentationKey --output tsv)

az webapp config appsettings set \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --settings APPINSIGHTS_INSTRUMENTATIONKEY=$INSTRUMENTATION_KEY

# ==============================================================================
# 6. CRIAÇÃO DO CONTAINER INSTANCE (RABBITMQ) COM PERSISTÊNCIA
# ==============================================================================
echo "Criando Container Instance para RabbitMQ ($ACI_NAME)..."
RABBITMQ_MNESIA_BASE="/var/lib/rabbitmq/mnesia" # Variável de correção de permissão

az container create \
    --resource-group $RESOURCE_GROUP \
    --name $ACI_NAME \
    --image $ACI_IMAGE \
    --dns-name-label $ACI_DNS_LABEL \
    --ports 5672 15672 \
    --location $LOCATION \
    --cpu 1 \
    --memory 1.5 \
    --os-type Linux \
    --environment-variables RABBITMQ_DEFAULT_USER=$RABBITMQ_USER RABBITMQ_DEFAULT_PASS=$RABBITMQ_PASS RABBITMQ_MNESIA_BASE=$RABBITMQ_MNESIA_BASE \
    --azure-file-volume-account-name $STORAGE_ACCOUNT_NAME \
    --azure-file-volume-share-name $RABBITMQ_SHARE_NAME \
    --azure-file-volume-mount-path /var/lib/rabbitmq \
    --azure-file-volume-account-key $STORAGE_KEY

# ==============================================================================
# 7. CONFIGURAÇÃO FINAL - VINCULANDO RABBITMQ AO WEB APP
# ==============================================================================
echo "Vinculando o FQDN do RabbitMQ ao Web App ($WEB_APP_NAME)..."

# Obtém o FQDN (Full Qualified Domain Name) do RabbitMQ ACI
RABBITMQ_HOST_FQDN=$(az container show \
    --resource-group $RESOURCE_GROUP \
    --name $ACI_NAME \
    --query ipAddress.fqdn -o tsv)

# Define as App Settings (variáveis de ambiente) no Web App
az webapp config appsettings set \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --settings spring.rabbitmq.host=$RABBITMQ_HOST_FQDN \
             spring.rabbitmq.port=5672 \
             spring.rabbitmq.username=$RABBITMQ_USER \
             spring.rabbitmq.password=$RABBITMQ_PASS \
             SPRING_DATASOURCE_URL="jdbc:sqlserver://$SQL_SERVER_NAME.database.windows.net:1433;database=$SQL_DB_NAME;user=$SQL_ADMIN_USER;password=$SQL_ADMIN_PASS;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;" \
             SPRING_DATASOURCE_USERNAME=$SQL_ADMIN_USER \
             SPRING_DATASOURCE_PASSWORD=$SQL_ADMIN_PASS \
             GROK_PASS="<SUA_CHAVE_GROK>" \
             APP_PASS_EMAIL="<SUA_SENHA_EMAIL>"

echo "Infraestrutura provisionada e RabbitMQ configurado!"
echo "FQDN do RabbitMQ (para teste UI): http://$RABBITMQ_HOST_FQDN:15672"