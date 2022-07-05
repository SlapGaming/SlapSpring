package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;

//JPA
@Embeddable

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoggedAttachment {
    private String name, extension, contentType;

    @Lob
    private byte[] content;

    @Builder.Default
    private boolean deleted = false;
}
