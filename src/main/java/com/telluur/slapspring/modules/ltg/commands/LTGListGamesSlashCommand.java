package com.telluur.slapspring.modules.ltg.commands;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import com.telluur.slapspring.abstractions.discord.paginator.PaginatorException;
import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.ltg.LTGListGamePaginatorService;
import com.telluur.slapspring.modules.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.modules.ltg.LTGUtil;
import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LTGListGamesSlashCommand extends ListenerAdapter implements ICommand {
    public static final String COMMAND_NAME = "listgames";
    public static final String COMMAND_DESCRIPTION = "Show all available Looking-To-Game roles.";
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setGuildOnly(true);
    private static final String LIST_GAMES_PAGINATOR_ID = "LIST-GAMES";
    private static final int PAGE_SIZE = 10;


    @Autowired
    BotSession session;

    @Autowired
    LTGGameRepository repo;

    @Autowired
    LTGQuickSubscribeService ltgQuickSubscribeService;

    @Autowired
    LTGListGamePaginatorService listGamePaginatorService;

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }


    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        boolean inLtgTx = event.getChannel().equals(session.getLTGTX());
        event.deferReply(!inLtgTx).queue();
        int startIndex = 0;
        MessageCreateData mcd;
        try {
            mcd = listGamePaginatorService.paginate(null, startIndex).toMessageCreateData();
        } catch (PaginatorException e) {
            MessageEmbed me = new EmbedBuilder()
                    .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                    .setTitle("Looking-To-Game Roles")
                    .setDescription("No Looking-To-Game roles were found.")
                    .build();
            mcd = new MessageCreateBuilder().setEmbeds(me).build();
        }
        event.getHook().sendMessage(mcd).queue();
    }
}

