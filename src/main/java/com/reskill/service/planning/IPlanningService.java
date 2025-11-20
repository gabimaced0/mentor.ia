package com.reskill.service.planning;

import com.reskill.dto.planning.CreatePlanningRequest;
import com.reskill.dto.planning.PlanningResponse;
import com.reskill.dto.planningItem.UpdatePlanningItemRequest;

import java.util.List;

public interface IPlanningService {

    PlanningResponse createPlanning(Long userId, CreatePlanningRequest request);

    List<PlanningResponse> getAllUserPlannings(Long userId);

    PlanningResponse getPlanningById(Long userId, Long planningId);

    PlanningResponse updatePlanningItem(Long userId, Long planningId, Long itemId, UpdatePlanningItemRequest request);

    void deletePlanning(Long userId, Long planningId);

    void deletePlanningItem(Long userId, Long planningId, Long itemId);
}
