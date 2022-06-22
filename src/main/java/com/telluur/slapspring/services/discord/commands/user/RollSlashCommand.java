package com.telluur.slapspring.services.discord.commands.user;

import com.telluur.slapspring.services.discord.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Random;

@Service
public class RollSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "roll";
    public static final String COMMAND_DESCRIPTION = "Roll a random number between 1 and <bound>, defaults to 1-100.";
    public static final String OPTION_BOUND_NAME = "bound";
    public static final String OPTION_BOUND_DESCRIPTION = "The upper bound";

    private static final OptionData boundOption = new OptionData(OptionType.INTEGER, OPTION_BOUND_NAME, OPTION_BOUND_DESCRIPTION, true).setMinValue(2);
    private static final CommandData commandData = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(boundOption).setDefaultEnabled(true);


    private final Random rand = new Random();

    @Nonnull
    @Override
    public CommandData data() {
        return commandData;
    }


    @Override
    public void handle(@Nonnull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Integer bound = event.getOption(OPTION_BOUND_NAME, 100, OptionMapping::getAsInt);

        int r = rand.nextInt(bound + 1);
        StringBuilder sb = new StringBuilder(String.format("Computer says `%d`", r));
        switch (r) {
            case 4 -> sb.append("\r\nhttps://imgs.xkcd.com/comics/random_number.png ");
            case 69 -> sb.append("\r\nnoice. ( ͡° ͜ʖ ͡°)");
        }
        event.getHook().sendMessage(sb.toString()).queue();
    }
}
