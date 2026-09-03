package com.example.ibsbms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_APPROVAL_ACTION")
public class ApprovalAction {

    @Id
    @Column(name = "ACTION_ID", nullable = false)
    private Long actionId;

    @Column(name = "REQUEST_ID", nullable = false)
    private Long requestId;

    @Column(name = "STAGE", length = 20, nullable = false)
    private String stage;

    @Column(name = "ACTION", length = 30, nullable = false)
    private String action;

    @Column(name = "ACTOR_ID", length = 60, nullable = false)
    private String actorId;

    @Column(name = "ACTOR_IP", length = 50)
    private String actorIp;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "ACTION_AT")
    private LocalDateTime actionAt;
}