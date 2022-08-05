package com.telluur.slapspring.modules.misc.discord.commands.admin;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class SaySlashCommand implements ICommand {
    public static final String COMMAND_NAME = "say";
    public static final String COMMAND_DESCRIPTION = "Echos a message to the channel";
    public static final String OPTION_MESSAGE_NAME = "message";
    public static final String OPTION_MESSAGE_DESCRIPTION = "The message to be echoed";

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOption(OptionType.STRING, OPTION_MESSAGE_NAME, OPTION_MESSAGE_DESCRIPTION, true)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);


    @Nonnull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }


    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        String payload = event.getOption(OPTION_MESSAGE_NAME, OptionMapping::getAsString);
        if (payload == null) {
            event.getHook().sendMessage("Please enter a valid message.").queue();
        } else {
            event.getChannel().sendMessage(payload).submit()
                    .thenCompose(ok -> event.getHook().sendMessage("Echoed your message.").submit());
        }
    }
}
