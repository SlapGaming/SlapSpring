package com.telluur.slapspring.modules.ltg.commands;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import com.telluur.slapspring.modules.ltg.LTGQuickSubscribeService;
import com.telluur.slapspring.modules.ltg.LTGRoleService;
import com.telluur.slapspring.modules.ltg.LTGUtil;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import java.util.stream.IntStream;


@Service
public class LTGSubscribeSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "subscribe";
    public static final String COMMAND_DESCRIPTION = "Join a @game role.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "The game role you want to join";
    public static final String OPTION_VAR_ROLE_DESCRIPTION = "The additional roles your want to join";


    private static final OptionData ROLE_OPTION = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + 1, OPTION_ROLE_DESCRIPTION, true);

    private static final List<OptionData> VAR_ROLE_OPTIONS = IntStream.range(2, 26)
            .mapToObj(i -> new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + i, OPTION_VAR_ROLE_DESCRIPTION, false))
            .toList();
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(ROLE_OPTION)
            .addOptions(VAR_ROLE_OPTIONS)
            .setGuildOnly(true);

    @Autowired
    LTGRoleService ltgRoleService;

    @Autowired
    LTGQuickSubscribeService quickSubscribeService;

    @Autowired
    Logger ltgLogger;


    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        List<Role> roles = IntStream.range(1, 26)
                .mapToObj(i -> event.getOption(OPTION_ROLE_NAME + i, null, OptionMapping::getAsRole))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (roles.size() <= 0) {
            MessageEmbed me = LTGUtil.failureEmbed("Uh-oh, somehow you didn't supply any roles... This should never happen..");
            event.getHook().sendMessageEmbeds(me).queue();
        } else {
            Member member = Objects.requireNonNull(event.getMember()); //Always in guild
            ltgRoleService.addMemberToRolesIfLTG(member, roles,
                    joinedRoles -> {
                        MessageEmbed me = LTGUtil.joinSuccessEmbed(joinedRoles);
                        event.getHook().sendMessageEmbeds(me).queue();

                        String broadcastMsg = LTGUtil.joinBroadcastMessage(member, joinedRoles);
                        quickSubscribeService.sendQuickSubscribe(joinedRoles, broadcastMsg);
                    },
                    fail -> {
                        MessageEmbed me = LTGUtil.joinFailEmbed(fail.getMessage());
                        event.getHook().sendMessageEmbeds(me).queue();
                    });
        }
    }
}
