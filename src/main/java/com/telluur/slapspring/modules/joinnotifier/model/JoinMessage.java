package com.telluur.slapspring.modules.joinnotifier.model;

import lombok.*;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//JPA
@Entity
@Table(name = "join_messages")

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString //No performance impact (No mappings or lazy loading fields).
public class JoinMessage {
    @Id
    private long id;

    @Nonnull
    private String formatString;
}
