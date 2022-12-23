package com.telluur.slapspring.modules.misc.discord.commands.restricted;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class KillSlashCommand extends AbstractRestrictedSlashCommand {
    public static final String COMMAND_NAME = "kill";
    public static final String COMMAND_DESCRIPTION = "Attempts a shutdown of the Spring container.";
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);

    @Autowired
    private ConfigurableApplicationContext springApplication;

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void systemHandle(SlashCommandInteractionEvent event) {
        event.reply("Shutting down JDA and Spring Container").setEphemeral(true).queue();

        event.getJDA().shutdown();
        springApplication.close();
        System.exit(0);
    }
}
