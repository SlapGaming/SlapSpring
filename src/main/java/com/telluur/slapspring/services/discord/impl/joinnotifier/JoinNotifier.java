package com.telluur.slapspring.services.discord.impl.joinnotifier;

import com.telluur.slapspring.services.discord.BotSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class JoinNotifier extends ListenerAdapter {
    private final BotSession botSession;

    public JoinNotifier(@Autowired BotSession bot) {
        this.botSession = bot;
    }

    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        botSession.getGeneralTX().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle(member.getEffectiveName())
                        .setDescription(String.format(JoinMessages.randomJoinMessage(), member.getAsMention()))
                        .setThumbnail(member.getEffectiveAvatarUrl()).build()
        ).queue();
    }


}
