package com.telluur.slapspring.services.discord.impl.ltg;

import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class LTGRoleService {

    @Autowired
    BotSession botSession;

    @Autowired
    LTGGameRepository repository;


    /**
     * Adds a role to a user when both the user and role are part of the guild and the role is an LTG role.
     * Adds the user to the community game role if they aren't already
     *
     * @param member  the member to add the role to
     * @param role    the role to be added
     * @param success success callback
     * @param failure failure callback
     */
    public void addMemberToRoleIfLTG(@Nonnull Member member, @Nonnull Role role, Consumer<Role> success, Consumer<Throwable> failure) {
        addMemberToRolesIfLTG(member, List.of(role), roles -> success.accept(roles.get(0)), failure);
    }


    /**
     * Adds roles to a user when both the user and roles are part of the guild and the role is an LTG role.
     * Adds the user to the community game role if they aren't already
     *
     * @param member  the member to add the role to
     * @param roles   the roles to be added
     * @param success success callback
     * @param failure failure callback
     */
    public void addMemberToRolesIfLTG(@Nonnull Member member, @Nonnull Collection<Role> roles, Consumer<List<Role>> success, Consumer<Throwable> failure) {
        List<Role> validRoles = roles.stream()
                .distinct()
                .filter(role -> repository.existsById(role.getIdLong()))
                .toList();

        if (validRoles.size() <= 0) {
            failure.accept(new IllegalArgumentException("No valid LTG roles provided"));
        } else {
            Guild guild = botSession.getBoundGuild();

            List<Role> joinedRoles = new ArrayList<>();
            Throwable lastThrow;
            validRoles.forEach(r -> guild.addRoleToMember(member, r).queue(ok -> joinedRoles.add(r)));

            if (joinedRoles.size() <= 0) {
                failure.accept(new IllegalAccessError("Discord API error"));
            } else {
                success.accept(joinedRoles);
                //Add community role
                Role communityRole = guild.getRoleById(botSession.getBotProperties().role_community());
                if (communityRole != null && !member.getRoles().contains(communityRole)) {
                    guild.addRoleToMember(member, communityRole).queue();
                }
            }
        }
    }
}
