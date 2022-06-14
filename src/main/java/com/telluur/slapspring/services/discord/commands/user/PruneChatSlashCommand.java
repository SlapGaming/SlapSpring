package com.telluur.slapspring.services.discord.commands.user;

import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PruneChatSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "prune";
    public static final String COMMAND_DESCRIPTION = "Deletes the last <limit> messages in a textchannel, skips pinned messages.";
    public static final String OPTION_LIMIT_NAME = "number";
    public static final String OPTION_LIMIT_DESCRIPTION = "The number of messages to delete";

    private static final OptionData limitOption = new OptionData(OptionType.INTEGER, OPTION_LIMIT_NAME, OPTION_LIMIT_DESCRIPTION, true).setRequiredRange(1, 100);
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(limitOption)
            .setDefaultEnabled(false);

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        Integer limit = event.getOption(OPTION_LIMIT_NAME, null, OptionMapping::getAsInt);

        if (limit == null || limit < 1 || limit > 100) {
            event.getHook().sendMessage("Please provide a valid limit between 1-200").queue();
            return;
        }

        event.getHook().sendMessage(String.format("Deleting last %d messages...", limit)).queue();

        TextChannel tx = event.getTextChannel();
        tx.getIterableHistory().limit(limit).submit()
                .thenCompose(messages -> {
                    List<Message> collect = messages.stream().filter(message -> !message.isPinned()).toList();
                    return tx.deleteMessages(collect).submit();
                })
                .whenComplete((ok, error) -> {
                    if (error != null) {
                        event.getHook().editOriginal(String.format("Uh-oh, something went wrong...(%s)", error.getMessage())).queue();
                    }
                });
    }
}
