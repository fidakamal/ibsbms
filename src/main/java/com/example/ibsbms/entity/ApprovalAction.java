package com.example.ibsbms.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "T_APPROVAL_ACTION")
public class ApprovalAction {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "approvalActionSeq"
    )
    @SequenceGenerator(
            name = "approvalActionSeq",
            sequenceName = "SEQ_APPROVAL_ACTION",
            allocationSize = 1
    )
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

    @Column(name = "ACTION_AT", nullable = false)
    private LocalDateTime actionAt;
}
