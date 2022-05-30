package com.telluur.slapspring.model.ltg;

import lombok.*;

import javax.persistence.*;


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

    @NonNull
    private String abbreviation, fullName;
}

