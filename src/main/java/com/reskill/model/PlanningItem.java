package com.reskill.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "TB_RS_PLANNING_ITEM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{planningitem.description.notblank}")
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "COMPLETED", nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNING_ID", nullable = false)
    private Planning planning;
}
