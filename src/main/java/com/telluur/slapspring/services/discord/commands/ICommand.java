package com.telluur.slapspring.services.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;

public interface ICommand {
    @Nonnull
    CommandData data();

    void handle(@Nonnull SlashCommandInteractionEvent event);
}
