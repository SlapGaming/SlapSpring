package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
import com.telluur.slapspring.services.discord.util.DiscordUtil;
import com.telluur.slapspring.services.discord.util.paginator.IPaginator;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.IntFunction;
import java.util.stream.Collectors;


@Service
public class ListGamesSlashCommand extends ListenerAdapter implements ICommand, IPaginator {
    public static final String COMMAND_NAME = "listgames";
    public static final String COMMAND_DESCRIPTION = "Show all available Looking-To-Game roles.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);
    private static final String LIST_GAMES_PAGINATOR_ID = "LISTGAMES";
    private static final int PAGE_SIZE = 10;


    @Autowired
    BotSession session;
    @Autowired
    LTGGameRepository repo;

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Message msg = createListMessage(0); //Fetch first page
        event.getHook().sendMessage(msg).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(PAGINATOR_BUTTON_PREFIX)) {
            event.deferEdit().queue();

            String indexString = buttonId.substring(PAGINATOR_BUTTON_PREFIX.length()); //Splits off the PAGINATOR_BUTTON_PREFIX from the following index
            Message msg;
            try {
                int index = Integer.parseInt(indexString);
                msg = createListMessage(index);


            } catch (NumberFormatException e) {
                msg = new MessageBuilder(LTGUtil.failureEmbed("Uh-oh, the paginator broke :(")).build();
            }
            event.getHook().editOriginal(msg).queue();
        }
    }


    private Message createListMessage(int index) {
        Guild guild = session.getBoundGuild();


        Page<LTGGame> games = fetchPage(index);

        if (games.hasContent()) {
            long totalGames = games.getTotalElements();


            String gamesList = games.get().map(ltgGame -> new HashMap.SimpleEntry<>(ltgGame, guild.getRoleById(ltgGame.getId())))
                    .filter(entry -> entry.getValue() != null)
                    .map(entry -> {
                        LTGGame game = entry.getKey();
                        return String.format(
                                        "`%-6s │ %-40s │ %2d subs`",
                                        game.getAbbreviation(),
                                        game.getFullName(),
                                        guild.getMembersWithRoles(entry.getValue()).size())
                                .replace(' ', DiscordUtil.NO_BREAK_SPACE);
                    }).collect(Collectors.joining("\r\n"));



            MessageEmbed me = new EmbedBuilder()
                    .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                    .setTitle(String.format("All %d Looking-To-Game roles", totalGames))
                    .setDescription(gamesList)
                    .setFooter("Use /gameinfo to see individual subscribers.") //0 indexed
                    .build();

            int currentPage = games.getNumber();
            int totalPages = games.getTotalPages();


            //Create back buttons, disable if on first page
            Button firstBtn = Button.secondary(PAGINATOR_BUTTON_PREFIX + PAGINATOR_INDEX.FIRST.index, "\u21E4 First");
            Button prevBtn = Button.secondary(PAGINATOR_BUTTON_PREFIX + (currentPage - 1), "\u2190 Prev");
            if (games.isFirst()) {
                firstBtn = firstBtn.asDisabled();
                prevBtn = prevBtn.asDisabled();
            }

            //Create disabled button indicating the page number
            Button currBtn = Button.primary(PAGINATOR_BUTTON_PREFIX + currentPage, String.format("Page %d/%d", currentPage + 1, totalPages)).asDisabled();

            //Create next buttons, disable if on last page
            Button nextBtn = Button.secondary(PAGINATOR_BUTTON_PREFIX + (currentPage + 1), "Next \u2192");
            Button lastBtn = Button.secondary(PAGINATOR_BUTTON_PREFIX + PAGINATOR_INDEX.LAST.index, "Last \u21E5");
            if (games.isLast()) {
                nextBtn = nextBtn.asDisabled();
                lastBtn = lastBtn.asDisabled();
            }


            return new MessageBuilder(me).setActionRows(ActionRow.of(firstBtn, prevBtn, currBtn, nextBtn, lastBtn)).build();


        } else {
            return new MessageBuilder(LTGUtil.failureEmbed("No roles found...")).build();
        }

    }


    /**
     * Fetched numbered page from the repository
     *
     * @param index 0 indexed page number,
     *              PAGINATOR_INDEX.FIRST.index (-1) for first page,
     *              PAGINATOR_INDEX.LAST.index (-2) for last page.
     * @return Pages containing LTGGames
     */
    private Page<LTGGame> fetchPage(int index) {
        /*
         * Calculates number of whole pages.
         * This equals the last page index in 0 indexed page system.
         * Safe cast: Unlikely to ever go over int bounds.
         */
        int lastIndex = ((int) repo.count()) / PAGE_SIZE;

        int safeIndex;
        if (index == PAGINATOR_INDEX.FIRST.index) {
            safeIndex = 0;
        } else if (index == PAGINATOR_INDEX.LAST.index || index >= lastIndex) {
            safeIndex = lastIndex;
        } else {
            safeIndex = index;
        }
        Pageable ltgPageable = PageRequest.of(safeIndex, PAGE_SIZE, Sort.by("abbreviation"));
        return repo.findAll(ltgPageable);
    }


    @Override
    public IntFunction<MessageEmbed> getPageProvider() {
        return (index) -> {
            Guild guild = session.getBoundGuild();


            Page<LTGGame> games = fetchPage(index);

            MessageEmbed me;
            if (games.hasContent()) {
                long totalGames = games.getTotalElements();


                String gamesList = games.get().map(ltgGame -> new HashMap.SimpleEntry<>(ltgGame, guild.getRoleById(ltgGame.getId())))
                        .filter(entry -> entry.getValue() != null)
                        .map(entry -> {
                            LTGGame game = entry.getKey();
                            return String.format(
                                            "`%-6s │ %-40s │ %2d subs`",
                                            game.getAbbreviation(),
                                            game.getFullName(),
                                            guild.getMembersWithRoles(entry.getValue()).size())
                                    .replace(' ', DiscordUtil.NO_BREAK_SPACE);
                        }).collect(Collectors.joining("\r\n"));

                MessageEmbed me = new EmbedBuilder()
                        .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                        .setTitle(String.format("All %d Looking-To-Game roles", totalGames))
                        .setDescription(gamesList)
                        .setFooter("Use /gameinfo to see individual subscribers.") //0 indexed
                        .build();

            } else {
                gamesList = Collections.emptyList();
            }
            return me;
        };
    }
}

