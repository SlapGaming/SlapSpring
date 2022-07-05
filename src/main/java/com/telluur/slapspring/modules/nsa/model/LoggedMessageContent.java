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
public class LoggedMessageContent {

    @Column(nullable = false)
    private String contentRaw;
}
