package com.telluur.slapspring.services.discord.impl.ltg;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class LTGUtil {
    public static final Color LTGSuccessColor = new Color(17, 128, 106);
    public static final Color LTGFailureColor = Color.RED;

    public static MessageEmbed createSuccessEmber(String message) {
        return new EmbedBuilder()
            .setColor(new Color(17, 128, 106))
            .setTitle("Looking-To-Game Success")
            .setDescription(String.format("Successfully joined %s.", message))
            .build();
    }

}
