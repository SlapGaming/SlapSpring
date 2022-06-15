package com.telluur.slapspring.services.discord.util.paginator;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.lang.NonNull;

import java.util.function.IntFunction;


public interface IPaginator {
    @NonNull
    String getPaginatorId();

    int getNumberOfTotalPages();

    @NonNull
    IntFunction<MessageEmbed> getPageProvider();
}
