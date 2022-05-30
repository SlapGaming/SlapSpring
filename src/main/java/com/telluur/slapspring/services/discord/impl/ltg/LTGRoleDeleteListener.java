package com.telluur.slapspring.services.discord.impl.ltg;

import com.telluur.slapspring.model.ltg.LTGGameRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LTGRoleDeleteListener extends ListenerAdapter {
    @Autowired
    LTGGameRepository gameRepository;

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Long deletedRoleID = event.getRole().getIdLong();
        if (gameRepository.existsById(deletedRoleID)) {
            gameRepository.deleteById(deletedRoleID);
            log.info("`{}` has been removed from the guild, and is no longer stored as LTG.", event.getRole().getName());
        }
    }
}
