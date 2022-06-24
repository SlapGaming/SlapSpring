package com.telluur.slapspring.abstractions.discord.paginator;


import com.telluur.slapspring.util.discord.DiscordUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Single listener implementation for a generic paginator service.
 * Classes implementing the coupled {@link IPaginator} interface are autowired and registered in this service.
 * <p>
 * <p>
 * The IDs set on the discord buttons pass persistent state on the requested paginator, and the requested page.
 * Paginator ID stucture: "PAGINATOR:[id]:[data]:[index]"
 * [id] hardcoded ID for a specific iPaginator
 * [data] allows for passing arbitrary data
 * [index] might contain hardcoded string values for the first/last page, or a positive integer.
 */

@Service
@Slf4j
public class PaginatorService extends ListenerAdapter {

    private final HashMap<String, IPaginator> paginators = new HashMap<>();


    public PaginatorService(@Autowired List<IPaginator> paginators) {
        log.info("Registering paginators: {}", paginators.stream()
                .map(paginator -> String.format("%s (%s)", paginator.getClass().getSimpleName(), paginator.getPaginatorId()))
                .toList());
        paginators.forEach(ip -> this.paginators.put(ip.getPaginatorId(), ip));
    }


    /**
     * CALLED BY JDA
     */
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null) {
            final Matcher matcher = PaginatorButtonUtil.FULL_BUTTON_REGEX.matcher(buttonId);
            boolean isValidButtonId = matcher.matches();
            if (isValidButtonId) { //Paginator button, have to handle response
                event.deferEdit().queue();

                /*
                Never null, since it passed the regex match.
                 */
                String paginatorId = Objects.requireNonNull(matcher.group(1));
                String data = Objects.requireNonNull(matcher.group(2));
                String indexString = Objects.requireNonNull(matcher.group(3));

                if (paginators.containsKey(paginatorId)) {
                    IPaginator paginator = paginators.get(paginatorId);

                    int totalPages = paginator.getNumberOfTotalPages(data);

                    int requestedIndex = switch (indexString) {
                        case PaginatorButtonUtil.PAGINATOR_FIRST_PAGE_INDEX -> 0;
                        case PaginatorButtonUtil.PAGINATOR_LAST_PAGE_INDEX -> totalPages - 1;
                        default -> Integer.parseInt(indexString); //Should never throw since it passed the regex match.
                    };

                    Message msg = null;
                    try {
                        msg = paginator.paginate(data, requestedIndex).toDiscordMessage();
                        event.getHook().editOriginal(msg).queue();
                    } catch (PaginatorException e) {
                        removePaginatorWithError(event, e.getMessage());
                    }

                } else {
                    removePaginatorWithError(event, "Uh-oh, the button was recognized as a paginator, but failed to find the page content.");
                }
            }
            //omitted else: id is not a paginator, not intended for this ListenerAdapter
        }
        //omitted else: id is null, not intended for this ListenerAdapter
    }

    private void removePaginatorWithError(@Nonnull ButtonInteractionEvent event, @Nonnull String message) {
        MessageEmbed me = new EmbedBuilder()
                .setColor(DiscordUtil.ERROR_COLOR)
                .setTitle("Paginator error!")
                .setDescription("Uh-oh, the button was recognized as a paginator, but failed to find the page content.")
                .build();
        Message msg = new MessageBuilder(me).build(); //removes the buttons.
        event.getHook().editOriginal(msg).queue();
    }

}
