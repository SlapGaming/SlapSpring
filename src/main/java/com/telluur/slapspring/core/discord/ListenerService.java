package com.telluur.slapspring.core.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * After construction of the JDA object, and the listeners depending on injection of the JDA bean,
 * registers the listeners automatically to tje JDA bean using injection.
 */
@Configuration
@Slf4j
public class ListenerService {
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
