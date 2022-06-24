package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

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

    @GeneratedValue
    @Id
    private UUID uuid = UUID.randomUUID();

    private String name;

    @Lob
    private byte[] content;


}
