package com.telluur.slapspring.modules.nsa.listeners;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.misc.discord.commands.user.AvatarSlashCommand;
import com.telluur.slapspring.modules.nsa.NSAUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

/**
 * Listener that logs changed avatars to the NSA channel
 * TODO: Log this to the DB as well
 */
@Service
public class AvatarUpdateListener extends ListenerAdapter {

    @Autowired
    private BotSession session;

    @Override
    public void onUserUpdateAvatar(@Nonnull UserUpdateAvatarEvent event) {
        User user = event.getUser();
        String url = user.getEffectiveAvatarUrl();
        String updateFormat = "The *user* avatar of %s [%s] was updated";
        session.getBoundGuild().retrieveMember(user).queue(
                member -> {
                    String title = String.format(updateFormat,
                            member.getEffectiveName(),
                            user.getEffectiveName()
                    );
                    logToNSA(title, url);
                },
                error -> {
                    String title = String.format(updateFormat,
                            user.getEffectiveName(),
                            user.getEffectiveName()
                    );
                    logToNSA(title, url);
                }
        );
    }

    @Override
    public void onGuildMemberUpdateAvatar(@Nonnull GuildMemberUpdateAvatarEvent event) {
        Member member = event.getMember();
        String title = String.format("The *guild* avatar of %s [%s] was updated",
                member.getEffectiveName(),
                member.getUser().getEffectiveName()
        );
        String url = member.getEffectiveAvatarUrl();
        logToNSA(title, url);
    }

    private void logToNSA(String title, String imageUrl) {
        MessageEmbed me = new EmbedBuilder()
                .setColor(NSAUtil.NSA_EDIT_COLOR)
                .setTitle(title)
                .setImage(String.format("%s%s", imageUrl, "?size=2048"))
                .setFooter(String.format("You can request the guild or user avatar using /%s", AvatarSlashCommand.COMMAND_NAME))
                .build();
        session.getNSATX().sendMessageEmbeds(me).queue();
    }
}
