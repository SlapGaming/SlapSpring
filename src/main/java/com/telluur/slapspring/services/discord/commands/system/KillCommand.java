package com.telluur.slapspring.services.discord.commands.system;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class KillCommand extends AbstractSystemCommand {
    public static final String COMMAND_NAME = "kill";
    public static final String COMMAND_DESCRIPTION = "Attempts a shutdown of the Spring container.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(false);

    @Autowired
    private ConfigurableApplicationContext springApplication;

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void systemHandle(SlashCommandInteractionEvent event) {
        event.reply("Attempting to shutdown Spring application...").setEphemeral(true).queue();

        //TODO this does not work...
        springApplication.close();
    }
}
