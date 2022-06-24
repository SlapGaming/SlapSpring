package com.telluur.slapspring.modules.ltg;

import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import com.telluur.slapspring.core.discord.BotSession;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
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
                .filter(role -> !member.getRoles().contains(role))
                .filter(role -> repository.existsById(role.getIdLong()))
                .toList();

        if (validRoles.size() <= 0) { //1 since community role was added.
            failure.accept(new IllegalArgumentException("The roles provided are either non-LTG roles, or you are already a subscribed."));
        } else {
            Guild guild = botSession.getBoundGuild();

            List<CompletableFuture<Role>> roleFutures = validRoles.stream().map(r -> {
                        CompletableFuture<Role> cf;
                        try {
                            cf = guild.addRoleToMember(member, r)
                                    .submit()
                                    //Return future with role when void stage succeeds.
                                    .thenApply(v -> r)
                                    //Return future with null when unchecked exception is thrown
                                    .exceptionally(e -> null);
                        } catch (IllegalArgumentException | InsufficientPermissionException | HierarchyException ignored) {
                            cf = CompletableFuture.completedFuture(null);
                        }
                        return cf;
                    }
            ).toList();

            //Create nonblocking callback for when all role adds complete.
            CompletableFuture.allOf(roleFutures.toArray(CompletableFuture[]::new))
                    .thenAccept(v -> {
                        List<Role> joinedRoles = roleFutures.stream()
                                .map(CompletableFuture::join) //This should be non-blocking due to allOf() callback
                                .filter(Objects::nonNull) //Filter out the error'd roles
                                .toList();

                        if (joinedRoles.size() <= 0) {
                            failure.accept(new IllegalAccessError("Discord API error, most likely bot permissions"));
                        } else {
                            success.accept(joinedRoles);
                            //Add community role
                            Role communityRole = guild.getRoleById(botSession.getBotProperties().role_community());
                            if (communityRole != null && !member.getRoles().contains(communityRole)) {
                                guild.addRoleToMember(member, communityRole).queue();
                            }
                        }
                    });
        }
    }

    public void removeMemberFromRolesIfLTG(@Nonnull Member member, @Nonnull Role role, Consumer<Role> success, Consumer<Throwable> failure) {
        removeMemberFromRolesIfLTG(member, List.of(role), roles -> success.accept(roles.get(0)), failure);
    }

    public void removeMemberFromRolesIfLTG(@Nonnull Member member, @Nonnull Collection<Role> roles, Consumer<List<Role>> success, Consumer<Throwable> failure) {
        List<Role> validRoles = roles.stream()
                .distinct()
                .filter(role -> member.getRoles().contains(role))
                .filter(role -> repository.existsById(role.getIdLong()))
                .toList();
        if (validRoles.size() <= 0) { //1 since community role was added.
            failure.accept(new IllegalArgumentException("The roles provided are either non-LTG roles, or you are already a unsubscribed."));
        } else {
            Guild guild = botSession.getBoundGuild();

            List<CompletableFuture<Role>> roleFutures = validRoles.stream().map(r -> {
                        CompletableFuture<Role> cf;
                        try {
                            cf = guild.removeRoleFromMember(member, r)
                                    .submit()
                                    //Return future with role when void stage succeeds.
                                    .thenApply(v -> r)
                                    //Return future with null when unchecked exception is thrown
                                    .exceptionally(e -> null);
                        } catch (IllegalArgumentException | InsufficientPermissionException | HierarchyException ignored) {
                            cf = CompletableFuture.completedFuture(null);
                        }
                        return cf;
                    }
            ).toList();

            //Create nonblocking callback for when all role adds complete.
            CompletableFuture.allOf(roleFutures.toArray(CompletableFuture[]::new))
                    .thenAccept(v -> {
                        List<Role> leftRoles = roleFutures.stream()
                                .map(CompletableFuture::join) //This should be non-blocking due to allOf() callback
                                .filter(Objects::nonNull) //Filter out the error'd roles
                                .toList();

                        if (leftRoles.size() <= 0) {
                            failure.accept(new IllegalAccessError("Discord API error, most likely bot permissions"));
                        } else {
                            success.accept(leftRoles);
                        }
                    });
        }
    }
}
