package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
import com.telluur.slapspring.services.discord.util.paginator.IPaginator;
import com.telluur.slapspring.services.discord.util.paginator.PaginatorException;
import com.telluur.slapspring.services.discord.util.paginator.PaginatorPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
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
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Objects;
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

    @Autowired
    LTGQuickSubscribeService ltgQuickSubscribeService;

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }


    @Override
    public @Nonnull String getPaginatorId() {
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
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int startIndex = 0;
        Message msg;
        try {
            msg = paginate(null, startIndex).toDiscordMessage();
        } catch (PaginatorException e) {
            MessageEmbed me = new EmbedBuilder()
                    .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                    .setTitle("Looking-To-Game Roles")
                    .setDescription("No Looking-To-Game roles were found.")
                    .build();
            msg = new MessageBuilder(me).build();
        }
        event.getHook().sendMessage(msg).queue();

    }


    @Override
    public @Nonnull PaginatorPage paginate(String ignored, int index) throws PaginatorException {
        Guild guild = session.getBoundGuild();
        int totalPages = getNumberOfTotalPages(null);

        /*
        Make sure we always get an index with content; either round to first or last page.
         */
        int lastIndex = totalPages - 1;
        int safeIndex = Math.max(0, Math.min(index, lastIndex));

        Pageable ltgPageable = PageRequest.of(safeIndex, PAGE_SIZE, Sort.by("abbreviation"));
        Page<LTGGame> ltgGames = repo.findAll(ltgPageable);

        if (ltgGames.hasContent()) {
            String gamesListString = ltgGames.get()
                    .map(ltgGame -> guild.getRoleById(ltgGame.getId()))
                    .filter(Objects::nonNull)
                    .map(role -> String.format("- %s [%d subscribers]",
                            role.getAsMention(),
                            guild.getMembersWithRoles(role).size()))
                    .collect(Collectors.joining("\n"));

            MessageEmbed embed = new EmbedBuilder()
                    .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                    .setTitle("Looking-To-Game Roles")
                    .setDescription(String.format("""
                                    **Total Looking-To-Game roles:** `%d`
                                                                            
                                    **Roles:**
                                    %s""",
                            getNumberOfTotalGames(),
                            gamesListString))
                    .setFooter(String.format("Use /%s to see individual subscribers.", GameInfoSlashCommand.COMMAND_NAME))
                    .build();

            PaginatorPage.Builder pageBuilder = PaginatorPage.builder()
                    .paginatorId(LIST_GAMES_PAGINATOR_ID)
                    .index(safeIndex)
                    .totalPages(totalPages)
                    .messageEmbed(embed);
            ltgQuickSubscribeService.createQSActionRowWithLTGGames(ltgGames.getContent())
                    .ifPresent(pageBuilder::additionalActionRow); //Add Quicksubscribe if possible.
            return pageBuilder.build();
        } else {
            throw new PaginatorException("Uh-oh, somehow the page you requested was empty...");
        }
    }
}

