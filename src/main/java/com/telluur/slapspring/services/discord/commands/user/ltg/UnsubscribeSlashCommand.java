package com.telluur.slapspring.services.discord.commands.user.ltg;

import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.commands.ICommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
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

import java.util.Objects;


@Service
public class UnsubscribeSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "unsubscribe";
    public static final String COMMAND_DESCRIPTION = "Silently leave a @game role.";
    public static final String OPTION_ROLE_NAME = "role";
    public static final String OPTION_ROLE_DESCRIPTION = "The game role you want to leave";


    private static final OptionData roleOption = new OptionData(OptionType.ROLE, OPTION_ROLE_NAME, OPTION_ROLE_DESCRIPTION, true);
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(roleOption);

    @Autowired
    LTGGameRepository repo;

    @Autowired
    Logger ltgLogger;

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Role role = Objects.requireNonNull(event.getOption(OPTION_ROLE_NAME, OptionMapping::getAsRole)); //Should never be null as the argument is required.
        if (repo.existsById(role.getIdLong())) {
            Guild guild = Objects.requireNonNull(event.getGuild()); //Should never be null as command is limited to guild.
            Member member = Objects.requireNonNull(event.getMember()); //Should never be null as command is limited to guild.
            guild.removeRoleFromMember(member, role).queue(
                    ok -> {
                        event.getHook().sendMessageFormat("You are now unsubscribed from %s", role.getAsMention()).queue();
                    },
                    fail -> {
                        //This should only happen when the bot does not have sufficient permissions.
                        event.getHook().sendMessageFormat("Failed to leave %s.", role.getAsMention()).queue();
                        ltgLogger.error("Could not assign role {} to {}", role.getName(), member.getEffectiveName(), fail);
                    });
        } else {
            event.replyFormat("Failed to leave %s. Not a LTG role.", role.getAsMention()).setEphemeral(true).queue();
        }
    }
}
