package com.reskill.mapper;

import com.reskill.dto.skill.SkillRequest;
import com.reskill.dto.skill.SkillResponse;
import com.reskill.model.Skill;
import com.reskill.model.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class SkillMapper {

    public Skill toEntity(SkillRequest request, User user) {
        return Skill.builder()
                .name(request.name())
                .user(user)
                .build();
    }


    public SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getUser().getName()
        );
    }
}
