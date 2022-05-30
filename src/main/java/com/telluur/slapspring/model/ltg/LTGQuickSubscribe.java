package com.telluur.slapspring.model.ltg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ltg_quicksubscribe")
public class LTGQuickSubscribe {
    @Id
    private long id; //This is the message id

    @Nonnull
    private long roleId;
}
