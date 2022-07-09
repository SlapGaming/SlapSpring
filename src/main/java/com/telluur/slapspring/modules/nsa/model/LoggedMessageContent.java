package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

//JPA
@Embeddable


//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoggedMessageContent {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String contentRaw;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date(System.currentTimeMillis());
}
