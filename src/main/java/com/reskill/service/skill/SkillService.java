package com.reskill.service.skill;

import com.reskill.dto.skill.SkillRequest;
import com.reskill.dto.skill.SkillResponse;
import com.reskill.exception.exceptions.ResourceNotFoundException;
import com.reskill.exception.exceptions.UnauthorizedAccessException;
import com.reskill.mapper.SkillMapper;
import com.reskill.model.Skill;
import com.reskill.model.User;
import com.reskill.repository.SkillRepository;
import com.reskill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final SkillMapper mapper;

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Skill findSkillOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    @Override
    public SkillResponse create(SkillRequest request, Long loggedUserId) {

        User user = findUserOrThrow(loggedUserId);

        Skill skill = mapper.toEntity(request, user);
        Skill saved = skillRepository.save(skill);

        return mapper.toResponse(saved);
    }

    @Override
    public List<SkillResponse> listByUser(Long loggedUserId) {

        User user = findUserOrThrow(loggedUserId);

        return skillRepository.findByUser(user)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public SkillResponse update(Long skillId, SkillRequest request, Long loggedUserId) {

        Skill skill = findSkillOrThrow(skillId);

        if (!skill.getUser().getId().equals(loggedUserId)) {
            throw new UnauthorizedAccessException("Not allowed to modify this skill");
        }

        skill.setName(request.name());
        Skill updated = skillRepository.save(skill);

        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long skillId, Long loggedUserId) {

        Skill skill = findSkillOrThrow(skillId);

        if (!skill.getUser().getId().equals(loggedUserId)) {
            throw new UnauthorizedAccessException("Not allowed to delete this skill");
        }

        skillRepository.delete(skill);
    }
}
