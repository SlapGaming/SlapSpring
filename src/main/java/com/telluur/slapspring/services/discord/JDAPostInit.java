package com.telluur.slapspring.services.discord;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JDAPostInit {
    @Autowired
    List<ListenerAdapter> listeners;
    @Autowired
    private JDA jda;


    @PostConstruct
    public void attachListeners() {
        log.info("Attaching listeners: {}", listeners.stream()
                .map(listenerAdapter -> listenerAdapter.getClass().getSimpleName())
                .collect(Collectors.toList())
        );
        listeners.forEach(l -> jda.addEventListener(l));
    }
}
