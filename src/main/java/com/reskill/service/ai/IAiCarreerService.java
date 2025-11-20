package com.reskill.service.ai;

import java.util.List;

public interface IAiCarreerService {

    AiRecommendation generateCareerPlan(String goal, List<String> skills);

    record AiRecommendation(
            String recommendationText,
            List<String> suggestedItems
    ) {}
}
