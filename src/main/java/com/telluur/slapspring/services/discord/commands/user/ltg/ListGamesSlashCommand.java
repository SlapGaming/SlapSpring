package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.stereotype.Service;

//@Service
public class ListGamesSlashCommand implements ICommand {
    @Override
    public CommandData data() {
        return null;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {

    }
}
