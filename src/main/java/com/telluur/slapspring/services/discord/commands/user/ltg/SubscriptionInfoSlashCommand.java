package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.util.paginator.IPaginator;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

//@Service
public class SubscriptionInfoSlashCommand implements ICommand, IPaginator {
    @Override
    public CommandData data() {
        return null;
    }

    @Override
    public String getPaginatorId() {
        return null;
    }

    @Override
    public int getNumberOfTotalPages() {
        return 0;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {

    }

    @Override
    public MessageEmbed getPage(int index) {
        return null;
    }
}
