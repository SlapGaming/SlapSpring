package com.telluur.slapspring.modules.bitrate;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import com.telluur.slapspring.modules.nsa.NSAUtil;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ForceMaxBitRateSlashCommand implements ICommand {
    public static final String COMMAND_NAME = "forcemaxbitrate";
    public static final String COMMAND_DESCRIPTION = "Force sets max bitrate on all voice channels";
    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);


    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }


    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        Guild guild = Objects.requireNonNull(event.getGuild()); //Checked by guildonly
        int maxBitrate = guild.getMaxBitrate();
        int boostTier = guild.getBoostTier().getKey();

        MessageEmbed me = new EmbedBuilder()
                .setColor(NSAUtil.NSA_EDIT_COLOR)
                .setTitle("Bit Rate")
                .setDescription(String.format("""
                                Attempting to set max bitrate (%d kbps) on all voice channels.
                                                                
                                **Current Boost Tier**: %d (%d kbps)
                                """,
                        toKbps(maxBitrate),
                        boostTier, toKbps(maxBitrate)))
                .setFooter("Any failures will be reported below.")
                .build();
        event.getHook().sendMessageEmbeds(me).queue();


        event.getGuild().getVoiceChannels()
                .forEach(vc -> {
                    long oldBitrate = vc.getBitrate();
                    vc.getManager().setBitrate(maxBitrate).queue(
                            ok -> {
                                event.getHook()
                                        .sendMessageFormat("Successfully set %d kbps on %s, was %d kbps.",
                                                toKbps(maxBitrate),
                                                vc.getAsMention(),
                                                toKbps(oldBitrate)
                                        )
                                        .setEphemeral(true)
                                        .queue();
                            },
                            failure -> {
                                event.getHook()
                                        .sendMessageFormat("Failed to set max bitrate on %s, was %d kbps.",
                                                toKbps(maxBitrate),
                                                vc.getAsMention(),
                                                toKbps(oldBitrate)
                                        )
                                        .setEphemeral(true)
                                        .queue();
                            });
                });
    }


    private long toKbps(long bitrate) {
        return bitrate / 1000;
    }

}
