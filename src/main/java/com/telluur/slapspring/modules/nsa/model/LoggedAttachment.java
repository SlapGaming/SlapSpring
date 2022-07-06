package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;

//JPA
@Entity
@Table(name = "nsa_logged_attachments")

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoggedAttachment {
    @Id
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loggedMessage_id")
    private LoggedMessage loggedMessage;

    private String name, extension, contentType;

    @Lob
    private byte[] content;

    @Builder.Default
    private boolean deleted = false;
}
