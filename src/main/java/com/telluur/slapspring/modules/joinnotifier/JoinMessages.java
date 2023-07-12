package com.telluur.slapspring.modules.joinnotifier;

import com.vdurmont.emoji.EmojiParser;

import java.util.Random;

public class JoinMessages {
    private static final Random random = new Random();
    private static final String JOIN_EMOJI = EmojiParser.parseToUnicode(":raising_hand:");
    private static final String[] messages = {
            "%1$s just joined the server - glhf!",
            "%1$s just joined. Everyone, look busy!",
            "%1$s just joined. Can I get a heal?",
            "%1$s joined. You must construct additional pylons.",
            "Welcome, %1$s. Stay awhile and listen.",
            "Welcome, %1$s. We were expecting you ( ͡° ͜ʖ ͡°)",
            "Welcome, %1$s. We hope you brought pizza.",
            "A wild %1$s appeared.",
            "Swoooosh. %1$s just landed.",
            "Brace yourselves. %1$s just joined the server.",
            "%1$s just joined. Hide your bananas.",
            "%1$s just slid into the server.",
            "A %1$s has spawned in the server.",
            "Big %1$s showed up!",
            "Where’s %1$s? In the server!",
            "%1$s hopped into the server. Kangaroo!!",
            "Challenger approaching - %1$s has appeared!",
            "It's a bird! It's a plane! Nevermind, it's just %1$s.",
            "We've been expecting you %1$s",
            "It's dangerous to go alone, take %1$s!",
            "%1$s has joined the server! It's super effective!",
            "Cheers, love! %1$s is here!",
            "%1$s is here, as the prophecy foretold.",
            "%1$s has arrived. Party's over.",
            "Ready player %1$s",
            "%1$s is here to kick butt and chew bubblegum. And %1$s is all out of gum.",
            "Hello. Is it %1$s you're looking for?",
            "%1$s has joined. Stay a while and listen!",
            "Roses are red, violets are blue, %1$s joined this server with you",
            "Welcome to the squeaky rubber ducky circlejerk, %1$s!",
            "Quickly! Welcome %1$s by throwing rubber duckies at them!"
    };

    public static String randomJoinMessage() {
        return String.format("%s %s", JOIN_EMOJI, messages[random.nextInt(messages.length)]);
    }
}
