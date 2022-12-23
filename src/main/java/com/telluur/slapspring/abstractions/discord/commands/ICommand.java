package com.telluur.slapspring.abstractions.discord.commands;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {
    @NonNull
    CommandData data();

    void handle(@NonNull SlashCommandInteractionEvent event);
}
