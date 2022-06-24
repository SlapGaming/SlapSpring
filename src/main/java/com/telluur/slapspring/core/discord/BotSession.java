package com.telluur.slapspring.core.discord;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BotSession {
    @Getter
    private final JDA jda;

    @Getter
    private final BotProperties botProperties;

    public BotSession(@Autowired BotProperties botProperties, @Autowired JDA jda) {
        this.botProperties = botProperties;
        this.jda = jda;
    }

    /*
    Convenience methods that returns the Guild/User/Role/Channel objects corresponding to the ID's
    specified in the bot properties. These are null checked here to remove null checks in all classes.
    Objects::requireNonNull should never fail, as the validity of the ID's are checked on startup.
    If it does, it means that the corresponding Guild/User/Role/Channel was deleted, or the bot no longer has access.
     */

    public Guild getBoundGuild() {
        return Objects.requireNonNull(jda.getGuildById(botProperties.guild()));
    }

    public TextChannel getGeneralTX() {
        return Objects.requireNonNull(jda.getTextChannelById(botProperties.tx_general()));
    }

    public TextChannel getLTGTX() {
        return Objects.requireNonNull(jda.getTextChannelById(botProperties.tx_ltg()));
    }

    public TextChannel getNSATX() {
        return Objects.requireNonNull(jda.getTextChannelById(botProperties.tx_nsa()));
    }

}

