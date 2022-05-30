package com.telluur.slapspring.services.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {
    CommandData data();

    void handle(SlashCommandInteractionEvent event);
}
