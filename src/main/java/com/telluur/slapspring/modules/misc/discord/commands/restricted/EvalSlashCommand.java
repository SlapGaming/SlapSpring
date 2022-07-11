package com.telluur.slapspring.modules.misc.discord.commands.restricted;

import com.telluur.slapspring.core.discord.BotSession;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Service
public class EvalSlashCommand extends AbstractRestrictedSlashCommand {
    public static final String COMMAND_NAME = "eval";
    public static final String COMMAND_DESCRIPTION = "Evaluates nashorn code, available bindings: jda, bot, event, guild, channel.";
    public static final String OPTION_NASHORN_NAME = "nashorn";
    public static final String OPTION_NASHORN_DESCRIPTION = "The nashorn code to be executed";

    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOption(OptionType.STRING, OPTION_NASHORN_NAME, OPTION_NASHORN_DESCRIPTION, true)
            .setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.DISABLED);

    @Autowired
    private BotSession botSession;

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void systemHandle(SlashCommandInteractionEvent event) {
        //TODO IMPLEMENT GRAALVM, This is broken
        event.deferReply().queue();

        String nashornCode = event.getOption(OPTION_NASHORN_NAME, OptionMapping::getAsString);

        ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", botSession);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("tx", event.getChannel());
        try {
            String executionResult = se.eval(nashornCode).toString();
            String reply = String.format("Evaluated Successfully:\r\n```\r\n%s```", executionResult);
            event.getHook().sendMessage(reply).queue();
        } catch (Exception e) {
            String reply = String.format("An exception was thrown:\r\n```\r\n%s```", e.getMessage());
            event.getHook().sendMessage(reply).queue();
        }
    }
}
