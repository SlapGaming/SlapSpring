package com.telluur.slapspring.modules.ltg;

import com.telluur.slapspring.abstractions.discord.paginator.IPaginator;
import com.telluur.slapspring.abstractions.discord.paginator.PaginatorException;
import com.telluur.slapspring.abstractions.discord.paginator.PaginatorPage;
import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.ltg.commands.LTGGameInfoSlashCommand;
import com.telluur.slapspring.modules.ltg.model.LTGGame;
import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LTGListGamePaginatorService implements IPaginator {

    private static final String LIST_GAMES_PAGINATOR_ID = "LIST-GAMES";
    private static final int PAGE_SIZE = 10;

    private final
    BotSession session;

    private final
    LTGGameRepository repo;

    private final
    LTGQuickSubscribeService quickSubService;

    public LTGListGamePaginatorService(@Autowired BotSession session,
                                       @Autowired LTGGameRepository repo,
                                       @Autowired @Lazy LTGQuickSubscribeService ltgQuickSubscribeService) {
        this.session = session;
        this.repo = repo;
        this.quickSubService = ltgQuickSubscribeService;
    }


    @Override
    public @NonNull String getPaginatorId() {
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
    public @NonNull PaginatorPage paginate(String ignored, int index) throws PaginatorException {
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
                    .setFooter(
                            String.format(
                                    "Use /%s to see individual subscribers or use the button(s)/dropdown below to subscribe to a Looking-To-Game role.",
                                    LTGGameInfoSlashCommand.COMMAND_NAME)
                    )
                    .build();

            PaginatorPage.Builder pageBuilder = PaginatorPage.builder()
                    .paginatorId(LIST_GAMES_PAGINATOR_ID)
                    .index(safeIndex)
                    .totalPages(totalPages)
                    .messageEmbed(embed);
            quickSubService.createQSActionRowWithLTGGames(ltgGames.getContent())
                    .ifPresent(pageBuilder::additionalActionRow); //Add Quicksubscribe if possible.
            return pageBuilder.build();
        } else {
            throw new PaginatorException("Uh-oh, somehow the page you requested was empty...");
        }
    }


}