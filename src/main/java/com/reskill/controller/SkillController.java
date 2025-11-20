package com.reskill.controller;

import com.reskill.dto.skill.SkillRequest;
import com.reskill.dto.skill.SkillResponse;
import com.reskill.model.User;
import com.reskill.service.skill.ISkillService;
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
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final ISkillService skillService;

    @Operation(
            summary = "Create a new skill",
            description = "Creates a new skill for the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Skill created successfully")
            }
    )
    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal User loggedUser
    ) {
        SkillResponse response = skillService.create(request, loggedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "List logged user skills",
            description = "Returns all skills of the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List returned successfully")
            }
    )
    @GetMapping
    public ResponseEntity<List<SkillResponse>> listByUser(
            @AuthenticationPrincipal User loggedUser
    ) {
        List<SkillResponse> list = skillService.listByUser(loggedUser.getId());
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Update a skill",
            description = "Updates a skill if it belongs to the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Skill updated"),
                    @ApiResponse(responseCode = "403", description = "Skill does not belong to user"),
                    @ApiResponse(responseCode = "404", description = "Skill not found")
            }
    )
    @PutMapping("/{skillId}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal User loggedUser
    ) {
        SkillResponse updated = skillService.update(skillId, request, loggedUser.getId());
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete a skill",
            description = "Deletes a skill if it belongs to the logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Skill deleted"),
                    @ApiResponse(responseCode = "403", description = "Skill does not belong to user"),
                    @ApiResponse(responseCode = "404", description = "Skill not found")
            }
    )
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long skillId,
            @AuthenticationPrincipal User loggedUser
    ) {
        skillService.delete(skillId, loggedUser.getId());
        return ResponseEntity.noContent().build();
    }
}
