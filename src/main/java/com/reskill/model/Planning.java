package com.reskill.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TB_RS_PLANNING")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{planning.goal.notblank}")
    @Column(nullable = false)
    private String goal;

    @NotBlank(message = "{planning.recommendation.notblank}")
    @Lob
    @Column(nullable = false)
    private String recommendation;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;


    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanningItem> items = new ArrayList<>();
}
