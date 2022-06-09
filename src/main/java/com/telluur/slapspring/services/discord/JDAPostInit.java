package com.telluur.slapspring.services.discord;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class JDAPostInit {

    private final Logger logger = LoggerFactory.getLogger(JDAPostInit.class);
    @Autowired
    List<ListenerAdapter> listeners;
    @Autowired
    private JDA jda;


    @PostConstruct
    public void attachListeners() {
        logger.info("Attaching listeners: {}", listeners.stream()
                .map(listenerAdapter -> listenerAdapter.getClass().getSimpleName())
                .collect(Collectors.toList())
        );
        listeners.forEach(l -> jda.addEventListener(l));
    }
}
