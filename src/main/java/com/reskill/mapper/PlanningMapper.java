package com.reskill.mapper;


import com.reskill.dto.planning.CreatePlanningRequest;
import com.reskill.dto.planning.PlanningResponse;
import com.reskill.dto.planningItem.PlanningItemResponse;
import com.reskill.model.Planning;
import com.reskill.model.PlanningItem;
import com.reskill.model.User;
import org.springframework.stereotype.Component;

@Component
public class PlanningMapper {

    public Planning toEntity(CreatePlanningRequest request, User user, String recommendationText) {
        Planning planning = new Planning();
        planning.setGoal(request.goal());
        planning.setRecommendation(recommendationText);
        planning.setUser(user);
        return planning;
    }

    public PlanningResponse toResponse(Planning planning) {
        return new PlanningResponse(
                planning.getId(),
                planning.getGoal(),
                planning.getRecommendation(),
                planning.getCreatedAt(),
                planning.getUser().getId(),
                planning.getItems().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    public PlanningItemResponse toItemResponse(PlanningItem item) {
        return new PlanningItemResponse(
                item.getId(),
                item.getDescription(),
                item.isCompleted()
        );
    }
}
