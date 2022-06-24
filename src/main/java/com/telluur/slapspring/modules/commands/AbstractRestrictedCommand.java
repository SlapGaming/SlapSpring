package com.telluur.slapspring.modules.commands;

import com.telluur.slapspring.core.discord.BotProperties;
import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

abstract class AbstractRestrictedCommand implements ICommand {
    @Autowired
    BotProperties botProperties;

    /**
     * Implements the handle() by adding a bot side permissions check, either informing the user of denied access,
     * or delegating the InteractionEvent to systemHandle()
     *
     * @param event InteractionEvent dispatched by discord
     */
    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        if (botProperties.system_users().contains(event.getUser().getId())) {
            systemHandle(event);
        } else {
            event.reply("Access denied. This command is limited to the bot owner.").setEphemeral(true).queue();
        }
    }

    /***
     * Systemcommands should implement this instead of handle().
     * @param event InteractionEvent dispatched by discord
     */
    abstract void systemHandle(SlashCommandInteractionEvent event);
}
