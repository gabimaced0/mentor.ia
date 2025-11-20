package com.reskill.repository;

import com.reskill.model.Skill;
import com.reskill.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByUser(User user);
}
