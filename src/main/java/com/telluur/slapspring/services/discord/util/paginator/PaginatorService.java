package com.telluur.slapspring.services.discord.util.paginator;


import com.telluur.slapspring.services.discord.util.DiscordUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String PAGINATOR_PREFIX = "PAGINATOR";
    private static final String PAGINATOR_FIRST_PAGE_INDEX = "FIRST";
    private static final String PAGINATOR_LAST_PAGE_INDEX = "LAST";
    private static final String ANY_EXCLUDING_COLON_REGEX = "[^:]+?";
    private static final String INTEGER_REGEX = "\\d+?";

    private static final Pattern FULL_BUTTON_REGEX = Pattern.compile(String.format("%s:(%s):(%s):(%s|%s|%s)",
            PAGINATOR_PREFIX, //paginator indicator
            ANY_EXCLUDING_COLON_REGEX, //[id]
            ANY_EXCLUDING_COLON_REGEX, //[data]
            INTEGER_REGEX, //[index]
            PAGINATOR_FIRST_PAGE_INDEX, //[index]
            PAGINATOR_LAST_PAGE_INDEX)); //[index]


    private final HashMap<String, IPaginator> paginators = new HashMap<>();


    public PaginatorService(@Autowired List<IPaginator> paginators) {
        log.info("Registering paginators: {}", paginators.stream()
                .map(paginator -> String.format("%s (%s)", paginator.getClass().getSimpleName(), paginator.getPaginatorId()))
                .toList());
        paginators.forEach(ip -> this.paginators.put(ip.getPaginatorId(), ip));
    }



    /**
     * CALLED BY JDA
     *
     * @param event
     */
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null) {
            final Matcher matcher = FULL_BUTTON_REGEX.matcher(buttonId);
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
                        case PAGINATOR_FIRST_PAGE_INDEX -> 0;
                        case PAGINATOR_LAST_PAGE_INDEX -> totalPages - 1;
                        default -> Integer.parseInt(indexString); //Should never throw since it passed the regex match.
                    };

                    MessageEmbed embed = paginator.paginate(data, requestedIndex);
                    Message msg = createPaginatorMessage(embed, paginatorId, data, requestedIndex, totalPages);
                    event.getHook().editOriginal(msg).queue();
                } else {
                    MessageEmbed me = new EmbedBuilder()
                            .setColor(DiscordUtil.ERROR_COLOR)
                            .setTitle("Paginator error!")
                            .setDescription("Uh-oh, the button was recognized as a paginator, but failed to find the page content.")
                            .build();
                    Message msg = new MessageBuilder(me).build(); //removes the buttons.
                    event.getHook().editOriginal(msg).queue();
                }
            }
            //omitted else: id is not a paginator, not intended for this ListenerAdapter
        }
        //omitted else: id is null, not intended for this ListenerAdapter
    }


    /*
    Utility methods
     */

    /**
     * Builds a button for a paginator with target index
     *
     * @param paginatorId corresponding paginator
     * @param index       the requested index target
     * @return formatted button id
     */
    public static String buildButton(@Nonnull String paginatorId, @Nonnull String data, int index) {
        return String.format("%s:%s:%s:%d", PAGINATOR_PREFIX, paginatorId, data, index);
    }

    /**
     * Builds a button for a paginator with target index set to first
     *
     * @param paginatorId corresponding paginator
     * @return formatted button id
     */
    public static String buildFirstButton(@Nonnull String paginatorId, @Nonnull String data) {
        return String.format("%s:%s:%s:%s", PAGINATOR_PREFIX, paginatorId, data, PAGINATOR_FIRST_PAGE_INDEX);
    }

    /**
     * Builds a button for a paginator with target index set to last
     *
     * @param paginatorId corresponding paginator
     * @return formatted button id
     */
    public static String buildLastButton(@Nonnull String paginatorId, @Nonnull String data) {
        return String.format("%s:%s:%s:%s", PAGINATOR_PREFIX, paginatorId, data, PAGINATOR_LAST_PAGE_INDEX);
    }
}
