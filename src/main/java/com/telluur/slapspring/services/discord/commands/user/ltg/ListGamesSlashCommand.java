package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
import com.telluur.slapspring.services.discord.util.TextUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class ListGamesSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "listgames";
    public static final String COMMAND_DESCRIPTION = "Show all available Looking-To-Game roles.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);


    private static final String PAGINATOR_BUTTON_PREFIX = LTGUtil.LTG_INTERACTABLE_PREFIX + "LG:PAGE:";

    /**
     * Maybe do this manually?
     * The chewtils paginator seems kinda shit NGL
     * <p>
     * - Use a Spring PagingAndSortingRepo + Pageable to dynamically fetch the menu pages.
     * - Timeout won't be neccesary for this as the buttons can hold persistent IDs, just a redraw on every button press.
     * - Could still use the eventwaiter just for button timeout...
     */


    @Autowired
    LTGGameRepository repo;

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Guild guild = Objects.requireNonNull(event.getGuild()); //Always in guild


        Page<LTGGame> games = fetchPage(0);

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
                                .replace(' ', TextUtil.SPACE);
                    }).collect(Collectors.joining("\r\n", "`", "`"));

            int currentPage = games.getNumber();
            int totalPages = games.getTotalPages();

            MessageEmbed me = new EmbedBuilder()
                    .setColor(LTGUtil.LTGSuccessColor)
                    .setTitle(String.format("All %d Looking-To-Game roles", totalGames))
                    .setDescription(gamesList)
                    .setFooter(String.format("Page %d/%d", currentPage + 1, totalPages)) //0 indexed
                    .build();

            event.getHook().sendMessageEmbeds(me).queue();
            //TODO paginator buttons

        } else {
            MessageEmbed me = LTGUtil.failureEmbed("No roles found...");
            event.getHook().sendMessageEmbeds(me).queue();
        }
    }


    /**
     * Fetched numbered page from the repository
     *
     * @param pagenum 0 indexed page number
     * @return Pages containing LTGGames
     */
    private Page<LTGGame> fetchPage(int pagenum) {
        Pageable ltgPageable = PageRequest.of(pagenum, 10, Sort.by("abbreviation"));
        Page<LTGGame> pagedGames = repo.findAll(ltgPageable);
        pagedGames.
        return pagedGames;
    }
}
