package com.telluur.slapspring.core.discord;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.annotation.Nonnull;
import java.util.List;

@ConfigurationProperties(prefix = "jda")
@ConstructorBinding
public record BotProperties(@Nonnull @ToString.Exclude String token, @Nonnull String status_type,
                            @Nonnull String status_message, @Nonnull String guild, @Nonnull List<String> system_users,
                            @Nonnull String role_admin, @Nonnull String role_mod, @Nonnull String role_community,
                            @Nonnull String tx_general, @Nonnull String tx_ltg, @Nonnull String tx_nsa) {
}
