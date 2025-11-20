package com.reskill.repository;

import com.reskill.model.Planning;
import com.reskill.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanningRepository extends JpaRepository<Planning, Long> {

    List<Planning> findByUser(User user);
    Optional<Planning> findByIdAndUserId(Long planningId, Long userId);
}
