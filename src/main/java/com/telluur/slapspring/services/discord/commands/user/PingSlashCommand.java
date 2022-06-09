package com.telluur.slapspring.services.discord.commands.user;

import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PingSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "ping";
    public static final String COMMAND_DESCRIPTION = "Simple ping pong command.";
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION).setDefaultEnabled(true);

    private static final String[] puns = {
            "What do you serve but not eat? \n" +
                    "A ping pong ball. ",
            "What do you call a girl standing in the middle of a table tennis court? \n" +
                    "Annette ",
            "Why are fish not good at ping pong? \n" +
                    "They don't like getting close to the net. ",
            "What did one ping pong ball say to the other ping pong ball? \n" +
                    "\"See you round..\"",
            "Why are spiders great ping pong players?\n" +
                    "Cause they have great topspin.",
            "What happens when you use pickles for a ping pong game? \n" +
                    "You get a volley of the Dills. \n",
            "When does a ping pong player go to sleep? \n" +
                    "Around Tennish. ",
            "What's a horse's favorite sport? \n" +
                    "Stable Tennis. ",
            "My ping pong opponent was not happy with my serve. He kept returning it. ",
            "Ping Pong: 10% of the time hitting a ping pong ball, 90% of the time chasing the ball around the room. ",
            "Are you a ping pong table? Cuz you ping pong my balls. ",
            "Stop staring at my \"Balls of Fury\". "
    };
    private final Random random = new Random();

    @Override
    public CommandData data() {
        return commandData;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        long gatewayPing = event.getJDA().getGatewayPing();

        event.getJDA().getRestPing().submit()
                .thenCompose(restPing -> {
                    String reply = String.format("%s\r\nREST Ping: `%dms`\r\nGateway ping: `%dms`",
                            puns[random.nextInt(puns.length)],
                            restPing,
                            gatewayPing);
                    return event.getHook().sendMessage(reply).submit();
                });
    }
}
