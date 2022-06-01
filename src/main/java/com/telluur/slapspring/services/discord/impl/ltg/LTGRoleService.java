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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
        log.info("LTG:A");
        List<Role> validRoles = roles.stream()
                .distinct()
                .filter(role -> repository.existsById(role.getIdLong()))
                .toList();

        if (validRoles.size() <= 1) { //1 since community role was added.
            failure.accept(new IllegalArgumentException("No valid LTG roles provided"));
        } else {
            Guild guild = botSession.getBoundGuild();


            guild.modifyMemberRoles(member, validRoles, null).queue(
                    v -> {},
                    e -> {}
            );


            //NEW

            List<CompletableFuture<Role>> roleFutures = validRoles.stream().map(r ->
                    guild.addRoleToMember(member, r)
                            .submit() //This stage has type void
                            //.handle((v, e) -> e != null ? null : r) //When role add was successful, return CF<Role>, else CF<null>
                            .thenApply(v -> r)
                            .exceptionally(e -> null)
            ).toList();

            log.info("LTG Post role adds submits");

            CompletableFuture.allOf(roleFutures.toArray(CompletableFuture[]::new))
                    .thenAccept(v -> {
                        List<Role> joinedRoles = roleFutures.stream()
                                .map(CompletableFuture::join) //This is nonBlocking since due to allOf() callback
                                .filter(Objects::nonNull) //Filter out the error'd roles
                                .toList();

                        log.info("LTG JOINED {}", joinedRoles.size());

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
                    });
            log.info("LTG Post allof()");
        }
    }

}
