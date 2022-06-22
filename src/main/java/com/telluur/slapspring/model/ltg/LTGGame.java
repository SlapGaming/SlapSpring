package com.telluur.slapspring.model.ltg;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


//JPA
@Entity
@Table(name = "ltg_game")

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString //No performance impact (No mappings or lazy loading fields).
public class LTGGame {
    @Id
    private long id;


    private @NonNull String abbreviation, fullName;

    private String description;
}

