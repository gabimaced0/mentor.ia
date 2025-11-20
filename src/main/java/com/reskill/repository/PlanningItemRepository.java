package com.reskill.repository;

import com.reskill.model.Planning;
import com.reskill.model.PlanningItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanningItemRepository extends JpaRepository<PlanningItem, Long> {

    List<PlanningItem> findByPlanning(Planning planning);
}
