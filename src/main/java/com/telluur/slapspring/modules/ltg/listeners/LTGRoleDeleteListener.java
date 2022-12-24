package com.telluur.slapspring.modules.ltg.listeners;

import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener that automatically removes an LTG role binding from the DB when it is deleted in Discord.
 */
@Service
@Slf4j
public class LTGRoleDeleteListener extends ListenerAdapter {
    @Autowired
    LTGGameRepository gameRepository;

    @Autowired
    Logger ltgLogger;

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Long deletedRoleID = event.getRole().getIdLong();
        if (gameRepository.existsById(deletedRoleID)) {
            gameRepository.deleteById(deletedRoleID);
            ltgLogger.info("LTG role `{}` with id `{}` has been removed from the guild, and is no longer stored as LTG.",
                    event.getRole().getName(), deletedRoleID);
        }
    }
}
