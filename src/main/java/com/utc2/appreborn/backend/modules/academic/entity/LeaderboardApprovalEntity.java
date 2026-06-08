package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_approval")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardApprovalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "semester_id", nullable = false, unique = true)
    private Long semesterId;
    @Column(name = "approved_by", nullable = false)
    private Long approvedBy;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}