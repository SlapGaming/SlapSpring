package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
import com.telluur.slapspring.services.discord.util.paginator.IPaginator;
import com.telluur.slapspring.services.discord.util.paginator.PaginatorService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class GameInfoSlashCommand implements ICommand, IPaginator {
    public static final String COMMAND_NAME = "gameinfo";
    public static final String COMMAND_DESCRIPTION = "Show more information about a Looking-To-Game role, including all subscribers.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "Looking-To-Game role";
    private static final OptionData ROLE_OPTION = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME, OPTION_ROLE_DESCRIPTION, true);
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).addOptions(ROLE_OPTION).setDefaultEnabled(true);
    private static final String GAME_INFO_PAGINATOR_ID = "GAME-INFO";
    private static final int PAGE_SIZE = 10;

    @Autowired
    BotSession session;

    @Autowired
    LTGGameRepository repository;

    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption(OPTION_ROLE_NAME, OptionMapping::getAsRole)); //Assumes working front end validation

        if (repository.existsById(role.getIdLong())) {
            event.deferReply().queue();

            String roleIdString = role.getId(); //Persistent data passed on button
            int startingIndex = 0;
            Message paginatorMessage = PaginatorService.createPaginatorMessage(
                    paginate(roleIdString, 0),
                    GAME_INFO_PAGINATOR_ID,
                    roleIdString,
                    startingIndex,
                    getNumberOfTotalPages(role)
            );
            event.getHook().sendMessageEmbeds(paginatorMessage).queue();
        } else {
            MessageEmbed me = LTGUtil.failureEmbed(String.format("%s is not a Looking-To-Game role.", role.getAsMention()));
            event.replyEmbeds(me).setEphemeral(true).queue();
        }
    }

    @Override
    public @NotNull String getPaginatorId() {
        return GAME_INFO_PAGINATOR_ID;
    }

    private List<Member> getSubscribers(Role role) {
        return session.getBoundGuild().getMembersWithRoles(role);
    }

    private int getNumberOfTotalPages(Role role) {
        int count = getSubscribers(role).size();
        int pages = count / PAGE_SIZE; //int division
        if (count % PAGE_SIZE > 0) {
            pages += 1; //Account for non-full last page
        }
        return pages;
    }

    @Override
    public int getNumberOfTotalPages(String data) {
        Role role = parseData(data);
        if (role != null) {
            return getNumberOfTotalPages(role);
        } else {
            //Default to returning 0.
            return 0;
        }
    }

    @Override
    public @NotNull MessageEmbed paginate(String data, int index) {
        Role role = parseData(data);

        if(role != null && repository.existsById(role.getIdLong())){
            getSubscribers(role).stream().sorted(
                    (final Role r1, final Role r2) -> r1.getName().compareTo(r2.getName())
            )

            return null;
        }else{
            LTGUtil.failureEmbed("Could not find the Looking-To-Game role. Has it been deleted?");
        }
    }

    @Nullable
    private Role parseData(String data) {
        return session.getBoundGuild().getRoleById(data);
    }
}
