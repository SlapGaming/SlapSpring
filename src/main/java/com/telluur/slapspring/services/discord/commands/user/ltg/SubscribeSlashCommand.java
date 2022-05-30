package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.services.discord.impl.ltg.LTGRoleService;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class SubscribeSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "subscribe";
    public static final String COMMAND_DESCRIPTION = "Join a @game role.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "The game role you want to join";
    public static final String OPTION_VAR_ROLE_DESCRIPTION = "The additional roles your want to join";


    private static final OptionData roleOption = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + 1, OPTION_ROLE_DESCRIPTION, true);

    private static final List<OptionData> varRoleOptions = IntStream.range(2, 25)
            .mapToObj(i -> new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + i, OPTION_VAR_ROLE_DESCRIPTION, false))
            .toList();
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(roleOption)
            .addOptions(varRoleOptions);

    @Autowired
    LTGRoleService ltgRoleService;

    @Autowired
    LTGQuickSubscribeService quickSubscribeService;

    @Autowired
    Logger ltgLogger;


    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        List<Role> roles = IntStream.range(1, 25)
                .mapToObj(i -> event.getOption(OPTION_ROLE_NAME + i, null, OptionMapping::getAsRole))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (roles.size() <= 0) {
            event.getHook().sendMessage("Uh-oh, somehow you didn't supply any roles... This should never happen..").queue();
        } else {
            Member member = Objects.requireNonNull(event.getMember()); //Always in guild
            ltgRoleService.addMemberToRolesIfLTG(member, roles,
                    joinedRoles -> {
                        String roleNames = joinedRoles.stream()
                                .map(IMentionable::getAsMention)
                                .collect(Collectors.joining(", "));
                        event.getHook().sendMessageFormat("Successfully joined %s.", roleNames).queue();
                        String broadcastMsg = LTGQuickSubscribeService.memberJoinedBroadcastMessage(member, joinedRoles);
                        quickSubscribeService.sendQuickSubscribeWithMessage(joinedRoles, broadcastMsg);
                    },
                    fail -> {
                        event.getHook().sendMessageFormat("Failed to join LTG role(s): %s.", fail.getMessage()).queue();
                    });
        }
    }
}
