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
# 6. CRIAÇÃO DA INFRAESTRUTURA DE VM (RABBITMQ)
# ==============================================================================
echo "Criando Rede Virtual e IP Público..."
az network vnet create --resource-group $RESOURCE_GROUP --name $VNET_NAME --location $LOCATION --address-prefix 10.0.0.0/16 --subnet-name $SUBNET_NAME --subnet-prefix 10.0.0.0/24
az network public-ip create --resource-group $RESOURCE_GROUP --name $PUBLIC_IP_NAME --location $LOCATION --sku Standard --allocation-method Static

echo "Criando NSG para controle de Firewall..."
az network nsg create --resource-group $RESOURCE_GROUP --name $NSG_NAME

# Criar Regra de Firewall: Permitir SSH (22)
echo "Permitindo SSH (Porta 22) para acesso de manutenção à VM..."
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name AllowSSH \
    --priority 100 --direction Inbound --access Allow --protocol Tcp --destination-port-range 22 --source-address-prefix Internet

# Criar Regra de Firewall: Permitir AMQP (5672)
echo "Permitindo RabbitMQ AMQP (Porta 5672) para conexão do App Service..."
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name AllowRabbitMQAMQP \
    --priority 110 --direction Inbound --access Allow --protocol Tcp --destination-port-range 5672 --source-address-prefix Internet

# Criar Regra de Firewall: Permitir Management UI (15672)
echo "Permitindo RabbitMQ Management UI (Porta 15672) para acesso de gerenciamento..."
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name AllowRabbitMQWeb \
    --priority 120 --direction Inbound --access Allow --protocol Tcp --destination-port-range 15672 --source-address-prefix Internet

echo "Criando Máquina Virtual ($VM_NAME) e gerando chaves SSH..."
az vm create \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --image $VM_IMAGE \
    --size $VM_SIZE \
    --admin-username $VM_ADMIN_USER \
    --public-ip-address $PUBLIC_IP_NAME \
    --vnet-name $VNET_NAME \
    --subnet $SUBNET_NAME \
    --nsg $NSG_NAME \
    --generate-ssh-keys # Isso salva a chave privada na sua máquina (~/.ssh/id_rsa)

# Obter o IP Público da VM
VM_PUBLIC_IP=$(az network public-ip show --resource-group $RESOURCE_GROUP --name $PUBLIC_IP_NAME --query ipAddress --output tsv)
echo "IP Público da VM RabbitMQ: $VM_PUBLIC_IP"


# ==============================================================================
# 6.1 EXECUTAR CONFIGURAÇÃO REMOTA NA VM
# ==============================================================================
echo "Aguardando a inicialização da VM e configurando o RabbitMQ via SSH..."

# O comando SSH executa os passos: 1. Atualizar SO, 2. Instalar Docker, 3. Rodar RabbitMQ
az vm run-command invoke \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --command-id RunShellScript \
    --scripts "sudo apt update && \
               sudo apt install -y docker.io && \
               sudo docker run -d --name rabbitmq \
               -p 5672:5672 -p 15672:15672 \
               -e RABBITMQ_DEFAULT_USER=$RABBITMQ_USER \
               -e RABBITMQ_DEFAULT_PASS=$RABBITMQ_PASS \
               rabbitmq:3-management"

echo "RabbitMQ Container está sendo executado na VM em $VM_PUBLIC_IP."

# ==============================================================================
# 7. CONFIGURAÇÃO FINAL - VINCULANDO RABBITMQ AO WEB APP
# ==============================================================================
echo "Vinculando o IP Público da VM RabbitMQ ao Web App ($WEB_APP_NAME)..."

# Define as App Settings (variáveis de ambiente) no Web App
az webapp config appsettings set \
    --name $WEB_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --settings spring.rabbitmq.host=$VM_PUBLIC_IP \
             spring.rabbitmq.port=5672 \
             spring.rabbitmq.username=$RABBITMQ_USER \
             spring.rabbitmq.password=$RABBITMQ_PASS \
             # ... Mantenha suas outras configurações de SQL, Groq, E-mail aqui
             SPRING_DATASOURCE_URL="[SUA_URL_AZURE_SQL]" \
             SPRING_DATASOURCE_USERNAME="[SEU_USUARIO_SQL]" \
             SPRING_DATASOURCE_PASSWORD="[SUA_SENHA_SQL]" \
             GROK_PASS="[SUA_CHAVE_GROK]" \
             APP_PASS_EMAIL="[SUA_SENHA_EMAIL]"

echo "Infraestrutura provisionada e RabbitMQ na VM configurado com IP: $VM_PUBLIC_IP."