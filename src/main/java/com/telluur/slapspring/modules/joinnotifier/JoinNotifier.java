package com.telluur.slapspring.modules.joinnotifier;

import com.telluur.slapspring.core.discord.BotSession;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * Custom welcome service for new users joining the bound guild.
 * Displays their avatar in an embed along with a welcome pun
 * TODO move puns from hardcoded in source to DB.
 */
@Service
public class JoinNotifier extends ListenerAdapter {
    @Autowired
    private BotSession botSession;

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
