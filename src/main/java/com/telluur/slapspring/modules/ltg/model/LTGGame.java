package com.telluur.slapspring.modules.ltg.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Represents a Discord role used for LTG roles.
 */
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

    @NotNull
    @NonNull //javax.validation for hibernate table generation, lombok for runtime
    private String abbreviation, fullName;

    private String description;
}

