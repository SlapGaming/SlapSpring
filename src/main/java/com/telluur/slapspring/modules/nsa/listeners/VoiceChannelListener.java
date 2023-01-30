package com.telluur.slapspring.modules.nsa.listeners;

import com.telluur.slapspring.core.discord.BotSession;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoiceChannelListener extends ListenerAdapter {
    @Autowired
    BotSession session;

    @Override
    public void onGuildVoiceUpdate(@NonNull GuildVoiceUpdateEvent event) {
        Member member = event.getMember();
        AudioChannelUnion joined = event.getChannelJoined();
        AudioChannelUnion left = event.getChannelLeft();


        if (joined != null && left != null) {
            //User moved to another VC
            datelogFormat("**%s** moved from __%s__ to __%s__.", member.getEffectiveName(), left.getName(), joined.getName());
        } else if (joined != null) {
            //User newly connected to a VC
            datelogFormat("**%s** joined __%s__.", member.getEffectiveName(), joined.getName());
        } else if (left != null) {
            //User disconnected from all guild VCs
            datelogFormat("**%s** left __%s__.", member.getEffectiveName(), left.getName());
        }
    }

    /**
     * Sends a formatted message to the logging channel.
     *
     * @param format a String::format String
     * @param args   Arguments referenced by the format specifiers in the format string.
     *               If there are more arguments than format specifiers, the extra arguments are ignored.
     *               The number of arguments is variable and may be zero.
     *               The maximum number of arguments is limited by the maximum dimension of a Java array as defined by
     *               The Java Virtual Machine Specification.
     *               The behaviour on a null argument depends on the conversion.
     */
    private void datelogFormat(String format, Object... args) {
        String formatted = String.format(format, args);
        session.getNSATX().sendMessageFormat("%s | %s", TimeFormat.DATE_TIME_SHORT.now(), formatted).queue();
    }


    //TODO: Log all this in the DB, as it currently oly echoes to NSA
}
