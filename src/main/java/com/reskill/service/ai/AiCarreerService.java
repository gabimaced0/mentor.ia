package com.reskill.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiCarreerService implements IAiCarreerService {

    private final OpenAiChatModel openAiChatModel;

    @Override
    public AiRecommendation generateCareerPlan(String goal, List<String> skills) {
        String prompt = buildPrompt(goal, skills);


        String fullText = openAiChatModel.call(prompt);

        return parseAiResponse(fullText);
    }

    private String buildPrompt(String goal, List<String> skills) {
        return """
                Gere um planejamento de carreira baseado nas informações abaixo:

                Objetivo principal:
                - %s

                Skills do usuário:
                - %s

                Responda seguindo este formato:

                RECOMENDAÇÃO:
                (um texto com níveis iniciante, intermediário e avançado)

                ITENS DO PLANEJAMENTO:
                - liste entre 5 e 10 tarefas, um item por linha
                """.formatted(
                goal,
                String.join("\n- ", skills)
        );
    }

    private AiRecommendation parseAiResponse(String fullText) {
        if (fullText == null || fullText.isEmpty()) {
            return new AiRecommendation("Sem recomendação gerada.", new ArrayList<>());
        }

        String[] split = fullText.split("ITENS DO PLANEJAMENTO:");
        String recommendationText = split[0].replace("RECOMENDAÇÃO:", "").trim();

        List<String> items = new ArrayList<>();
        if (split.length > 1) {
            for (String line : split[1].split("\n")) {
                line = line.replace("-", "").trim();
                if (!line.isBlank()) items.add(line);
            }
        }

        return new AiRecommendation(recommendationText, items);
    }
}
