package com.telluur.slapspring.modules.ltg.commands;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import com.telluur.slapspring.abstractions.discord.paginator.IPaginator;
import com.telluur.slapspring.abstractions.discord.paginator.PaginatorException;
import com.telluur.slapspring.abstractions.discord.paginator.PaginatorPage;
import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.modules.ltg.LTGUtil;
import com.telluur.slapspring.modules.ltg.model.LTGGame;
import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LTGGameInfoSlashCommand implements ICommand, IPaginator {
    public static final String COMMAND_NAME = "gameinfo";
    public static final String COMMAND_DESCRIPTION = "Show more information about a Looking-To-Game role, including all subscribers.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "Looking-To-Game role";
    private static final OptionData ROLE_OPTION = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME, OPTION_ROLE_DESCRIPTION, true);
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(ROLE_OPTION) //TODO add autocomplete limiting to LTG roles
            .setGuildOnly(true);
    private static final String GAME_INFO_PAGINATOR_ID = "GAME-INFO";
    private static final int PAGE_SIZE = 10;

    @Autowired
    BotSession session;

    @Autowired
    LTGGameRepository repository;

    @Autowired
    LTGQuickSubscribeService quickSubscribeService;

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        Role role = Objects.requireNonNull(event.getOption(OPTION_ROLE_NAME, OptionMapping::getAsRole)); //Assumes working front end validation

        if (repository.existsById(role.getIdLong())) {
            boolean inLtgTx = event.getChannel().equals(session.getLTGTX());
            event.deferReply(!inLtgTx).queue();
            try {
                MessageCreateData mcd = paginate(role.getId(), 0).toMessageCreateData();
                event.getHook().sendMessage(mcd).queue();
            } catch (PaginatorException e) {
                MessageEmbed me = LTGUtil.failureEmbed(e.getMessage());
                event.replyEmbeds(me).queue();
            }

        } else {
            MessageEmbed me = LTGUtil.failureEmbed(String.format("%s is not a Looking-To-Game role.", role.getAsMention()));
            event.replyEmbeds(me).setEphemeral(true).queue();
        }
    }

    @Override
    public @NonNull String getPaginatorId() {
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
    public @NonNull PaginatorPage paginate(String data, int index) throws PaginatorException {
        Role role = parseData(data);

        if (role != null) {
            Optional<LTGGame> optionalLTGGame = repository.findById(role.getIdLong());
            if (optionalLTGGame.isPresent()) {
                String pageString = getSubscribers(role).stream()
                        .sorted(Comparator.comparing(Member::getEffectiveName))
                        .skip((long) index * PAGE_SIZE) //Skip items to get to the indexed page
                        .limit(PAGE_SIZE) //Max 10 items
                        .map(member -> String.format("- %s (%s#%s)",
                                member.getAsMention(),
                                member.getUser().getName(),
                                member.getUser().getDiscriminator()))
                        .collect(Collectors.joining("\r\n"));

                LTGGame ltgGame = optionalLTGGame.get();

                MessageEmbed me = new EmbedBuilder()
                        .setColor(LTGUtil.LTG_SUCCESS_COLOR)
                        .setTitle("Looking-To-Game Role Info")
                        .setDescription(String.format("""
                                        **Role:** %s
                                        **Total subscribers:** `%d`
                                                                                
                                        **Subscribers:**
                                        %s""",
                                role.getAsMention(),
                                getSubscribers(role).size(),
                                pageString))
                        .setFooter(
                                String.format(
                                        "Use /%s to see all Looking-To-Game roles or use the button below to subscribe to %s.",
                                        LTGListGamesSlashCommand.COMMAND_NAME, ltgGame.getAbbreviation())
                        )
                        .build();

                PaginatorPage.Builder pageBuilder = PaginatorPage.builder()
                        .paginatorId(GAME_INFO_PAGINATOR_ID)
                        .index(index)
                        .totalPages(getNumberOfTotalPages(role))
                        .data(data)
                        .messageEmbed(me);
                quickSubscribeService.createQSActionRowWithLTGGames(List.of(ltgGame))
                        .ifPresent(pageBuilder::additionalActionRow); //Add Quicksubscribe if possible.
                return pageBuilder.build();
            }
        }
        throw new PaginatorException("Could not find the Looking-To-Game role. Has it been deleted?");
    }

    @Nullable
    private Role parseData(String data) {
        return session.getBoundGuild().getRoleById(data);
    }
}
