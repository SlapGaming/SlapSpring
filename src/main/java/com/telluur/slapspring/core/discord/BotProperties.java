package com.telluur.slapspring.core.discord;

import lombok.NonNull;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@ConfigurationProperties(prefix = "jda")
@ConstructorBinding
public record BotProperties(@NonNull @ToString.Exclude String token, @NonNull String status_type,
                            @NonNull String status_message, @NonNull String guild, @NonNull List<String> system_users,
                            @NonNull String role_admin, @NonNull String role_mod, @NonNull String role_community,
                            @NonNull String tx_general, @NonNull String tx_ltg, @NonNull String tx_nsa,
                            @NonNull String web_base_url) {
}
