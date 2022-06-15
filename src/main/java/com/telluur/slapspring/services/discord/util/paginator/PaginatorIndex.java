package com.telluur.slapspring.services.discord.util.paginator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PaginatorIndex {
    FIRST(-2),
    LAST(-3),
    ERROR(-4);

    @Getter
    private final int index;


}
