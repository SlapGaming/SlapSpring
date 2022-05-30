package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Objects;

@Service
public class AddGameSlashCommand extends ListenerAdapter implements ICommand {
    public static final String COMMAND_NAME = "addgame";
    public static final String COMMAND_DESCRIPTION = "Pop up a modal to create a new LTG game.";
    public static final String MODAL_ID = "LTG_ADD_MODAL";
    public static final String MODAL_ABBRV = "LTG_ADD_ABBRV";
    public static final String MODAL_NAME = "LTG_ADD_DESC";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(false);

    @Autowired
    LTGGameRepository gameRepository;

    @Autowired
    BotSession botSession;

    @Autowired
    Logger ltgLogger;

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        TextInput abbrv = TextInput.create(MODAL_ABBRV, "Abbreviation", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, 6)
                .setPlaceholder("BF3")
                .build();

        TextInput name = TextInput.create(MODAL_NAME, "Full Game Name", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, 25)
                .setPlaceholder("Battlefield 3")
                .build();

        Modal modal = Modal.create(MODAL_ID, "Add an LTG Game")
                .addActionRows(ActionRow.of(abbrv), ActionRow.of(name))
                .build();

        event.replyModal(modal).queue();

    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        if (event.getModalId().equals(MODAL_ID)) {
            String abbreviation = Objects.requireNonNull(event.getValue(MODAL_ABBRV)).getAsString();
            String fullname = Objects.requireNonNull(event.getValue(MODAL_NAME)).getAsString();

            final List<String> ILLEGAL_CHARS = "|\"'`(){}[]<>/"
                    .chars()
                    .mapToObj(i -> Character.toString((char) i))
                    .toList();

            if (ILLEGAL_CHARS.stream().anyMatch(abbreviation::contains)) {
                event.replyFormat(
                        "Failed to create LTG Role.\r\nThe abbreviation contained one of the following illegal characters: `%s`",
                        ILLEGAL_CHARS).queue();
                return;
            }

            if (ILLEGAL_CHARS.stream().anyMatch(fullname::contains)) {
                event.replyFormat(
                        "Failed to create LTG Role.\r\nThe name contained one of the following illegal characters: `%s`",
                        ILLEGAL_CHARS).queue();
                return;
            }

            event.deferReply(true).queue();

            Objects.requireNonNull(event.getGuild()).createRole() //Guild cannot be null as the command is only available in the guild
                    .setName(String.format("%s | %s", abbreviation, fullname))
                    .setPermissions(Permission.EMPTY_PERMISSIONS)
                    .setMentionable(true)
                    .setColor(new Color(17, 128, 106))
                    .queue(
                            role -> {
                                LTGGame ltgGame = new LTGGame(role.getIdLong(), abbreviation, fullname);
                                gameRepository.save(ltgGame);
                                ltgLogger.info(String.format("Role `%s` with id `%s` created", role.getName(), role.getId()));
                                event.getHook().sendMessageFormat("Created LTG role, `%s`", role.getName()).queue();
                            },
                            fail -> {
                                event.getHook().sendMessageFormat("Failed to create LTG role, `%s | %s`", abbreviation, fullname).queue();
                            }
                    );
        }
    }
}
