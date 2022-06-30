package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

//JPA
@Entity
@Table(name = "nsa_logged_message")

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoggedMessage {
    @Id
    private long id;

    private int version = 0;

    @Column(nullable = false)
    private long channelId, userId;

    private String jumpUrl;

    @Column(nullable = false)
    private String contentRaw;


    @OneToMany(mappedBy="id")
    private List<LoggedAttachment> attachmentList;

}
