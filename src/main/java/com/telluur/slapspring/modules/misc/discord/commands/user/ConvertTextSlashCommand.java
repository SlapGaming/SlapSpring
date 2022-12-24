package com.telluur.slapspring.modules.misc.discord.commands.user;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

//TODO @Service
public class ConvertTextSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "convert";
    public static final String COMMAND_DESCRIPTION = "Converts plaintext to obscure unicode characters";
    public static final String OPTION_MEMBER_NAME = "text";
    public static final String OPTION_MEMBER_DESCRIPTION = "The plaintext to be converted";

    @Override
    public @NonNull CommandData data() {
        return null;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {

    }
}
