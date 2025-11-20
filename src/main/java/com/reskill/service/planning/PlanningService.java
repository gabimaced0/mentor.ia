package com.reskill.service.planning;

import com.reskill.dto.planning.CreatePlanningRequest;
import com.reskill.dto.planning.PlanningResponse;
import com.reskill.dto.planningItem.UpdatePlanningItemRequest;
import com.reskill.exception.exceptions.ResourceNotFoundException;
import com.reskill.exception.exceptions.UnauthorizedAccessException;
import com.reskill.mapper.PlanningMapper;
import com.reskill.model.Planning;
import com.reskill.model.PlanningItem;
import com.reskill.model.User;
import com.reskill.repository.PlanningItemRepository;
import com.reskill.repository.PlanningRepository;
import com.reskill.repository.UserRepository;
import com.reskill.service.ai.AiCarreerService;
import com.reskill.service.ai.IAiCarreerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService implements IPlanningService {

    private final UserRepository userRepository;
    private final PlanningRepository planningRepository;
    private final IAiCarreerService aiService;
    private final PlanningMapper mapper;

    @Override
    public PlanningResponse createPlanning(Long userId, CreatePlanningRequest request) {

        User user = loadUser(userId);
        var ai = aiService.generateCareerPlan(request.goal(), request.skills());

        Planning planning = mapper.toEntity(request, user, ai.recommendationText());


        if (planning.getItems() == null) {
            planning.setItems(new ArrayList<>());
        }

        for (String itemText : ai.suggestedItems()) {
            PlanningItem item = new PlanningItem();
            item.setDescription(itemText);
            item.setCompleted(false);
            item.setPlanning(planning);
            planning.getItems().add(item);
        }

        planningRepository.save(planning);
        return mapper.toResponse(planning);
    }

    @Override
    public List<PlanningResponse> getAllUserPlannings(Long userId) {
        User user = loadUser(userId);

        return planningRepository.findByUser(user).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public PlanningResponse getPlanningById(Long userId, Long planningId) {
        Planning planning = loadPlanning(planningId);
        ensureOwnership(userId, planning);
        return mapper.toResponse(planning);
    }

    @Override
    public PlanningResponse updatePlanningItem(Long userId, Long planningId, Long itemId, UpdatePlanningItemRequest request) {

        Planning planning = loadPlanning(planningId);
        ensureOwnership(userId, planning);

        PlanningItem item = planning.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Planning item not found."));

        item.setDescription(request.description());
        item.setCompleted(request.completed());

        planningRepository.save(planning);

        return mapper.toResponse(planning);
    }

    @Override
    public void deletePlanning(Long userId, Long planningId) {
        Planning planning = loadPlanning(planningId);
        ensureOwnership(userId, planning);
        planningRepository.delete(planning);
    }

    @Override
    public void deletePlanningItem(Long userId, Long planningId, Long itemId) {

        Planning planning = loadPlanning(planningId);
        ensureOwnership(userId, planning);

        Iterator<PlanningItem> iterator = planning.getItems().iterator();
        boolean removed = false;

        while (iterator.hasNext()) {
            PlanningItem item = iterator.next();
            if (item.getId().equals(itemId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new ResourceNotFoundException("Planning item not found.");
        }

        planningRepository.save(planning);
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private Planning loadPlanning(Long id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planning not found."));
    }

    private void ensureOwnership(Long userId, Planning planning) {
        if (!planning.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not own this planning.");
        }
    }
}
