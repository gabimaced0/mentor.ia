package com.reskill.controller;

import com.reskill.dto.planning.CreatePlanningRequest;
import com.reskill.dto.planning.PlanningResponse;
import com.reskill.dto.planningItem.UpdatePlanningItemRequest;
import com.reskill.model.User;
import com.reskill.service.planning.IPlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final IPlanningService planningService;

    @Operation(
            summary = "Create a new career planning",
            description = "Creates a career planning using AI suggestions for the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Planning created successfully")
            }
    )
    @PostMapping
    public ResponseEntity<PlanningResponse> createPlanning(
            @Valid @RequestBody CreatePlanningRequest request,
            @AuthenticationPrincipal User loggedUser
    ) {
        PlanningResponse created = planningService.createPlanning(loggedUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @Operation(
            summary = "List all plannings of logged user",
            description = "Returns all career plannings created by the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List returned successfully")
            }
    )
    @GetMapping
    public ResponseEntity<List<PlanningResponse>> listUserPlannings(
            @AuthenticationPrincipal User loggedUser
    ) {
        List<PlanningResponse> list = planningService.getAllUserPlannings(loggedUser.getId());
        return ResponseEntity.ok(list);
    }


    @Operation(
            summary = "Get planning by ID",
            description = "Returns a specific planning if it belongs to the logged user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Planning returned"),
                    @ApiResponse(responseCode = "404", description = "Planning not found")
            }
    )
    @GetMapping("/{planningId}")
    public ResponseEntity<PlanningResponse> getPlanning(
            @PathVariable Long planningId,
            @AuthenticationPrincipal User loggedUser
    ) {
        PlanningResponse response = planningService.getPlanningById(loggedUser.getId(), planningId);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Update a planning item",
            description = "Updates one specific planning item if it belongs to the logged user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item updated"),
                    @ApiResponse(responseCode = "404", description = "Planning or item not found"),
                    @ApiResponse(responseCode = "403", description = "Not allowed")
            }
    )
    @PutMapping("/{planningId}/item/{itemId}")
    public ResponseEntity<PlanningResponse> updatePlanningItem(
            @PathVariable Long planningId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdatePlanningItemRequest request,
            @AuthenticationPrincipal User loggedUser
    ) {
        PlanningResponse updated = planningService.updatePlanningItem(
                loggedUser.getId(),
                planningId,
                itemId,
                request
        );
        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "Delete planning",
            description = "Deletes an entire planning if it belongs to the logged user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Planning deleted"),
                    @ApiResponse(responseCode = "404", description = "Planning not found")
            }
    )
    @DeleteMapping("/{planningId}")
    public ResponseEntity<Void> deletePlanning(
            @PathVariable Long planningId,
            @AuthenticationPrincipal User loggedUser
    ) {
        planningService.deletePlanning(loggedUser.getId(), planningId);
        return ResponseEntity.noContent().build();
    }



    @Operation(
            summary = "Delete a planning item",
            description = "Deletes one item inside a planning if it belongs to the logged user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Item deleted"),
                    @ApiResponse(responseCode = "404", description = "Planning or item not found"),
                    @ApiResponse(responseCode = "403", description = "Not allowed")
            }
    )
    @DeleteMapping("/{planningId}/item/{itemId}")
    public ResponseEntity<Void> deletePlanningItem(
            @PathVariable Long planningId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User loggedUser
    ) {
        planningService.deletePlanningItem(loggedUser.getId(), planningId, itemId);
        return ResponseEntity.noContent().build();
    }
}
