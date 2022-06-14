package com.telluur.slapspring.services.discord.util.paginator;

import org.springframework.lang.NonNull;

import java.util.Collection;


public interface IPaginator {
    @NonNull
    String getPaginatorButtonPrefix();

    int getPaginatorPageSize();

    @NonNull
    String getEmbedHeaderText();

    @NonNull
    String getEmbedFooterText();

    @NonNull
    Collection<String> providePage(int index);



}
