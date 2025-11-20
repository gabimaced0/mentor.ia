package com.reskill.service.skill;

import com.reskill.dto.skill.SkillRequest;
import com.reskill.dto.skill.SkillResponse;

import java.util.List;

public interface ISkillService {

    SkillResponse create(SkillRequest request, Long loggedUserId);

    List<SkillResponse> listByUser(Long loggedUserId);

    SkillResponse update(Long skillId, SkillRequest request, Long loggedUserId);

    void delete(Long skillId, Long loggedUserId);
}
