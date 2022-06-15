package com.telluur.slapspring.services.discord.impl.ltg;

import com.telluur.slapspring.services.discord.util.DiscordUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LTGUtil {

    public static final String LTG_INTERACTABLE_PREFIX = "LTG:";
    public static final Color LTG_SUCCESS_COLOR = new Color(17, 128, 106);
    public static final Color LTG_FAILURE_COLOR = DiscordUtil.ERROR_COLOR;

    public static MessageEmbed successEmbed(String message) {
        return new EmbedBuilder()
                .setColor(LTG_SUCCESS_COLOR)
                .setTitle("Looking-To-Game Success")
                .setDescription(message)
                .build();
    }

    public static MessageEmbed failureEmbed(String message) {
        return new EmbedBuilder()
                .setColor(LTG_FAILURE_COLOR)
                .setTitle("Looking-To-Game Failure")
                .setDescription(message)
                .build();
    }

    public static MessageEmbed joinSuccessEmbed(Role role) {
        return joinSuccessEmbed(Collections.singleton(role));
    }

    public static MessageEmbed joinSuccessEmbed(Collection<Role> roles) {
        String roleNames = roles.stream()
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setColor(LTG_SUCCESS_COLOR)
                .setTitle("Looking-To-Game Success")
                .setDescription(String.format("Successfully joined Looking-To-Game role(s): %s.", roleNames))
                .build();
    }

    public static MessageEmbed joinFailEmbed(String reason) {
        return new EmbedBuilder()
                .setColor(LTG_FAILURE_COLOR)
                .setTitle("Looking-To-Game Failure")
                .setDescription(String.format("Failed to join: %s", reason))
                .build();
    }

    public static MessageEmbed leaveSuccessEmbed(Role role) {
        return leaveSuccessEmbed(Collections.singleton(role));
    }

    public static MessageEmbed leaveSuccessEmbed(Collection<Role> roles) {
        String roleNames = roles.stream()
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setColor(LTG_SUCCESS_COLOR)
                .setTitle("Looking-To-Game Success")
                .setDescription(String.format("Successfully left Looking-To-Game role(s): %s.", roleNames))
                .build();
    }

    public static MessageEmbed leaveFailureEmbed(String reason) {
        return new EmbedBuilder()
                .setColor(LTG_FAILURE_COLOR)
                .setTitle("Looking-To-Game Failure")
                .setDescription(String.format("Failed to leave: %s", reason))
                .build();
    }

    public static String joinBroadcastMessage(Member member, Role role) {
        return joinBroadcastMessage(member, Collections.singleton(role));
    }

    public static String joinBroadcastMessage(Member member, Collection<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::getName)
                .collect(Collectors.joining(", ", "`", "`"));
        return String.format("`%s` joined Looking-To-Game role(s): %s.", member.getEffectiveName(), rolesString);
    }

}
