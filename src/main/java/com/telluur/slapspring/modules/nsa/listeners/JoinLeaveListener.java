package com.telluur.slapspring.modules.nsa.listeners;

import com.telluur.slapspring.core.discord.BotSession;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;

@Service
public class JoinLeaveListener extends ListenerAdapter {

    @Autowired
    private BotSession session;


    @Override
    public void onGuildMemberJoin(@NonNull GuildMemberJoinEvent event) {
        MessageEmbed me = buildEmbed(
                event.getMember(),
                event.getUser(),
                Color.GREEN,
                "User has joined the server",
                "%s (%s) has joined the server.");
        session.getNSATX().sendMessageEmbeds(me).queue();


    }

    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {

        MessageEmbed me = buildEmbed(
                event.getMember(),
                event.getUser(),
                Color.RED,
                "User has left the server",
                "%s (%s) has left the server.");
        session.getNSATX().sendMessageEmbeds(me).queue();
    }


    private MessageEmbed buildEmbed(
            @Nullable Member member,
            @NonNull User user,
            @NonNull Color color,
            @NonNull String title,
            @NonNull String descriptionFormat
    ) {
        String name, mention, avatarUrl;
        if (member != null) {
            name = member.getEffectiveName();
            mention = member.getAsMention();
            avatarUrl = member.getEffectiveAvatarUrl();
        } else {
            name = user.getName();
            mention = user.getAsMention();
            avatarUrl = user.getEffectiveAvatarUrl();
        }

        return new EmbedBuilder()
                .setColor(color)
                .setTitle(title)
                .setThumbnail(avatarUrl)
                .setAuthor(name, null, avatarUrl)
                .setDescription(String.format(
                        descriptionFormat,
                        mention,
                        user.getName()))
                .setFooter(String.format("ID: %s", user.getId()))
                .setTimestamp(Instant.now())
                .build();
    }
}
