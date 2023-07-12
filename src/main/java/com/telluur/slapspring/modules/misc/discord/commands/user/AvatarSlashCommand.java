package com.telluur.slapspring.modules.misc.discord.commands.user;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Objects;

@Service
public class AvatarSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "avatar";
    public static final String COMMAND_DESCRIPTION = "Enlarges a user's avatar. Defaults to server avatar.";
    public static final String OPTION_MEMBER_NAME = "user";
    public static final String OPTION_MEMBER_DESCRIPTION = "The user requested";
    public static final String OPTION_TYPE_NAME = "type";
    public static final String OPTION_TYPE_DESCRIPTION = "Request the global or server avatar";
    public static final String OPTION_TYPE_CHOICE_GLOBAL = "global";
    public static final String OPTION_TYPE_CHOICE_GUILD = "server";


    private static final OptionData MEMBER_OPTION = new OptionData(OptionType.USER, OPTION_MEMBER_NAME, OPTION_MEMBER_DESCRIPTION, true);
    private static final OptionData TYPE_OPTION = new OptionData(OptionType.STRING, OPTION_TYPE_NAME, OPTION_TYPE_DESCRIPTION, false)
            .addChoice(OPTION_TYPE_CHOICE_GLOBAL, OPTION_TYPE_CHOICE_GLOBAL)
            .addChoice(OPTION_TYPE_CHOICE_GUILD, OPTION_TYPE_CHOICE_GUILD);

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(MEMBER_OPTION, TYPE_OPTION)
            .setGuildOnly(true);

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        //Validated by the discord frontend.
        Member member = Objects.requireNonNull(event.getOption(OPTION_MEMBER_NAME, OptionMapping::getAsMember));

        String type = event.getOption(OPTION_TYPE_NAME, OPTION_TYPE_CHOICE_GUILD, OptionMapping::getAsString);
        String url, title;
        title = "Server avatar";
        if (OPTION_TYPE_CHOICE_GUILD.equals(type)) {
            url = member.getEffectiveAvatarUrl();
        } else {
            title = "Global avatar";
            url = member.getUser().getEffectiveAvatarUrl();
        }


        MessageEmbed me = new EmbedBuilder()
                .setColor(Color.orange)
                .setTitle(String.format("%s | %s [%s]", title, member.getEffectiveName(), member.getUser().getEffectiveName()))
                .setImage(String.format("%s%s", url, "?size=2048"))
                .build();
        event.getHook().sendMessageEmbeds(me).queue();
    }
}
