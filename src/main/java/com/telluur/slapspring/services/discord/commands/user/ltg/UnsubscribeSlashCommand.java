package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.services.discord.commands.ICommand;
import com.telluur.slapspring.services.discord.impl.ltg.LTGRoleService;
import com.telluur.slapspring.services.discord.impl.ltg.LTGUtil;
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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


@Service
public class UnsubscribeSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "unsubscribe";
    public static final String COMMAND_DESCRIPTION = "Silently leave a @game role.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "The game role you want to leave";
    public static final String OPTION_VAR_ROLE_DESCRIPTION = "The additional roles your want to leave";

    private static final OptionData roleOption = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + 1, OPTION_ROLE_DESCRIPTION, true);

    private static final List<OptionData> varRoleOptions = IntStream.range(2, 26)
            .mapToObj(i -> new OptionData(OptionType.ROLE, OPTION_ROLE_NAME + i, OPTION_VAR_ROLE_DESCRIPTION, false))
            .toList();
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(roleOption)
            .addOptions(varRoleOptions)
            .setDefaultEnabled(true);

    @Autowired
    LTGRoleService ltgRoleService;

    @Autowired
    Logger ltgLogger;

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
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
            ltgRoleService.removeMemberFromRolesIfLTG(member, roles,
                    leftRoles -> {
                        MessageEmbed me = LTGUtil.leaveSuccessEmbed(leftRoles);
                        event.getHook().sendMessageEmbeds(me).queue();
                    },
                    fail -> {
                        MessageEmbed me = LTGUtil.leaveFailureEmbed(String.format("Failed to leave: %s", fail.getMessage()));
                        event.getHook().sendMessageEmbeds(me).queue();
                    });
        }
    }
}
