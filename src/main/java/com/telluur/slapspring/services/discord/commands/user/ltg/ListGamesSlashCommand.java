package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.EmbedPaginator;
import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;

//@Service
public class ListGamesSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "listgames";
    public static final String COMMAND_DESCRIPTION = "Show all available Looking-To-Game roles.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);

    EmbedPaginator ep = new EmbedPaginator.Builder()
            .setText("TODO").build();

    /**
     * Maybe do this manually?
     * The chewtils paginator seems kinda shit NGL
     *
     * - Use a Spring PagingAndSortingRepo + Pageable to dynamically fetch the menu pages.
     * - Timeout won't be neccesary for this as the buttons can hold persistent IDs, just a redraw on every button press.
     * - Could still use the eventwaiter just for button timeout...
     */



    @Autowired
    EventWaiter waiter;

    @Override
    public CommandData data() {
        return null;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {

    }
}
