package com.telluur.slapspring;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.telluur.slapspring.core.discord.BotProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SlapSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlapSpringApplication.class, args);
    }

    @Bean
    public JDA initJDA(@Autowired BotProperties botProperties, @Autowired EventWaiter waiter) throws LoginException, InterruptedException {
        //logger.info("Bot config: {}", botProperties.toString());
        return JDABuilder
                .create(botProperties.token(), EnumSet.allOf(GatewayIntent.class))
                .setActivity(Activity.of(Activity.ActivityType.COMPETING, "SPRING"))
                .addEventListeners(waiter)
                .build()
                .awaitReady();
    }

    @Bean
    public EventWaiter eventWaiter() {
        return new EventWaiter();
    }

}
