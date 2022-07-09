package com.telluur.slapspring.util.discord;

import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;

public class DiscordUtil {
    public static final char NO_BREAK_SPACE = '\u00A0'; //No break space character, cause discord collapses normal ones.

    public static final String ALWAYS_DISABLED_BUTTON_ID = "_IGNORED_";

    public static final Color SUCCESS_COLOR = Color.GREEN;
    public static final Color ERROR_COLOR = Color.RED;


    public static String channelTypeToString(ChannelType channelType) {
        return switch (channelType) {
            case TEXT -> "Text Channel";
            case VOICE -> "Voice Channel";
            case GUILD_PUBLIC_THREAD -> "Public Thread";
            case GUILD_PRIVATE_THREAD -> "Private Thread";
            default -> "Unknown Channel Type";
        };
    }

}
