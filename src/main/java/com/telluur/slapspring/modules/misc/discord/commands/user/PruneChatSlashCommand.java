package com.telluur.slapspring.modules.misc.discord.commands.user;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class PruneChatSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "prune";
    public static final String COMMAND_DESCRIPTION = "Deletes the last <limit> messages in a textchannel, skips pinned messages.";
    public static final String OPTION_LIMIT_NAME = "number";
    public static final String OPTION_LIMIT_DESCRIPTION = "The number of messages to delete";
    private static final int UPPER_LIMIT = 100;
    private static final OptionData limitOption = new OptionData(OptionType.INTEGER, OPTION_LIMIT_NAME, OPTION_LIMIT_DESCRIPTION, true).setRequiredRange(1, UPPER_LIMIT);
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(limitOption)
            .setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.DISABLED);

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        Integer limit = event.getOption(OPTION_LIMIT_NAME, null, OptionMapping::getAsInt);

        if (limit == null || limit < 1 || limit > UPPER_LIMIT) {
            event.getHook().sendMessageFormat("Please provide a valid limit between 1-%d", UPPER_LIMIT).queue();
            return;
        }

        event.getHook().sendMessage(String.format("Deleting last %d messages...", limit)).queue();

        TextChannel tx = event.getTextChannel();
        tx.getIterableHistory().limit(limit).queue(messages -> {
                    List<AuditableRestAction<Void>> collect = messages.stream()
                            .filter(message -> !message.isPinned())
                            .map(Message::delete)
                            .toList();
                    RestAction.allOf(collect).mapToResult().queue();
                }
        );
    }
}
