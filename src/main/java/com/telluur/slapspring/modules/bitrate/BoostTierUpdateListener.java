package com.telluur.slapspring.modules.bitrate;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.nsa.NSAUtil;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoostTierUpdateListener extends ListenerAdapter {
    @Autowired
    BotSession session;

    @Override
    public void onGuildUpdateBoostTier(@NonNull GuildUpdateBoostTierEvent event) {
        int oldTierKey = event.getOldBoostTier().getKey();
        int oldBitrate = event.getOldBoostTier().getMaxBitrate();
        int newTierKey = event.getNewBoostTier().getKey();
        int newBitrate = event.getNewBoostTier().getMaxBitrate();
        MessageEmbed me = new EmbedBuilder()
                .setColor(NSAUtil.NSA_EDIT_COLOR)
                .setTitle("Boost Tier Changed")
                .setDescription(String.format("""
                                Attempting to set max bitrate (%d kbps) on all voice channels.
                                                                
                                **Old Boost Tier**: %d (%d kbps)
                                **New Boost Tier**: %d (%d kbps)
                                """,
                        toKbps(newBitrate),
                        oldTierKey, toKbps(oldBitrate),
                        newTierKey, toKbps(newBitrate)))
                .setFooter("Any failures will be reported below.")
                .build();
        session.getNSATX().sendMessageEmbeds(me).queue();

        event.getGuild().getVoiceChannels().stream()
                .filter(vc -> vc.getBitrate() < newBitrate)
                .forEach(vc -> {
                    vc.getManager().setBitrate(newBitrate).queue(
                            ok -> {
                                session.getNSATX()
                                        .sendMessageFormat("Successfully set %d kbps on %s, was %d kbps.",
                                                toKbps(newBitrate),
                                                vc.getAsMention(),
                                                toKbps(oldBitrate)
                                        )
                                        .queue();
                            },
                            failure -> {
                                session.getNSATX()
                                        .sendMessageFormat("Failed to set %d kbps on %s, was %d kbps - @here.",
                                                toKbps(newBitrate),
                                                vc.getAsMention(),
                                                toKbps(oldBitrate)
                                        )
                                        .queue();
                            });
                });
    }

    private long toKbps(long bitrate) {
        return bitrate / 1000;
    }
}
