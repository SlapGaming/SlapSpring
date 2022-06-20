package com.telluur.slapspring.services.discord.commands.user.ltg.listgames;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
import com.telluur.slapspring.services.discord.util.DiscordUtil;
import com.telluur.slapspring.services.discord.util.paginator.IPaginator;
import com.telluur.slapspring.services.discord.util.paginator.PaginatorService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.stream.Collectors;


@Service
public class ListGamesSlashCommand extends ListenerAdapter implements ICommand, IPaginator {
    public static final String COMMAND_NAME = "listgames";
    public static final String COMMAND_DESCRIPTION = "Show all available Looking-To-Game roles.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);
    private static final String LIST_GAMES_PAGINATOR_ID = "LIST-GAMES";
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
    @NonNull
    public String getPaginatorId() {
        return LIST_GAMES_PAGINATOR_ID;
    }

    /**
     * Using a preset PAGE_SIZE in this class, calculate the total number of pages.
     *
     * @return number of pages
     */
    @Override
    public int getNumberOfTotalPages(String ignored) {
        int count = getNumberOfTotalGames();
        int pages = count / PAGE_SIZE; //int division
        if (count % PAGE_SIZE > 0) {
            pages += 1; //Account for non-full last page
        }
        return pages;
    }

    /**
     * Returns the number of LTG games in the DB
     * Internally this returns a long, the cast to int is relatively safe,
     * as the number of games is unlikely to exceed int bounds.
     *
     * @return number of games
     */
    private int getNumberOfTotalGames() {
        return (int) repo.count();
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int startIndex = 0;
        Message paginatorMessage = PaginatorService.createPaginatorMessage(
                paginate(null, startIndex),
                LIST_GAMES_PAGINATOR_ID,
                null, //No need for persistent data passing
                startIndex,
                getNumberOfTotalPages(null));
        event.getHook().sendMessage(paginatorMessage).queue();
    }


    @Override
    @NonNull
    public MessageEmbed paginate(String ignored, int index) {
        Guild guild = session.getBoundGuild();

        int count = getNumberOfTotalGames();
        int lastIndex = getNumberOfTotalPages(null) - 1;

        /*
        Make sure we always get an index with content; either round to first or last page.
         */
        int safeIndex;
        if (index < 0) {
            safeIndex = 0;
        } else if (index > lastIndex) {
            safeIndex = lastIndex;
        } else {
            safeIndex = index;
        }
        Pageable ltgPageable = PageRequest.of(safeIndex, PAGE_SIZE, Sort.by("abbreviation"));
        Page<LTGGame> games = repo.findAll(ltgPageable);

        if (games.hasContent()) {
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

            return new EmbedBuilder()
                    .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                    .setTitle(String.format("All %d Looking-To-Game roles", count))
                    .setDescription(gamesList)
                    .setFooter("Use /gameinfo to see individual subscribers.")
                    .build();
        } else {
            return LTGUtil.failureEmbed("Uh-oh, somehow the page you requested was empty...");
        }
    }
}

