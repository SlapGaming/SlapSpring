package com.telluur.slapspring.services.discord.util.paginator;


import com.telluur.slapspring.services.discord.util.DiscordUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

/**
 * Single listener implementation for a generic paginator service.
 * The IDs set on the discord buttons pass persistent state on the requested paginator, and the requested page.
 * Paginator ID stucture: "PAGINATOR:[PAGINATOR_PREFIX]:[REQUESTED_PAGE_INT]"
 */

@Service
public class PaginatorService extends ListenerAdapter {


    private final HashMap<String, IPaginator> paginators = new HashMap<>();


    public PaginatorService(@Autowired List<IPaginator> iPaginators) {
        for (IPaginator iPaginator : iPaginators) {
            paginators.put(iPaginator.getPaginatorId(), iPaginator);
        }
    }


    public static Message attachPaginator(String paginatorId, int currentIndex, int totalPages, MessageEmbed messageEmbed) {
        //Create back buttons, disable if on first page
        Button firstBtn = Button.secondary(PaginatorIdUtil.buildButtonId(paginatorId, PaginatorIndex.FIRST.getIndex()), "\u21E4 First");
        Button prevBtn = Button.secondary(PaginatorIdUtil.buildButtonId(paginatorId, currentIndex - 1), "\u2190 Prev");
        if (currentIndex == 0) {
            firstBtn = firstBtn.asDisabled();
            prevBtn = prevBtn.asDisabled();
        }

        //Create disabled button indicating the page number
        Button currBtn = Button.primary(DiscordUtil.ALWAYS_DISABLED_BUTTON_ID, String.format("Page %d/%d", currentIndex + 1, totalPages)).asDisabled();

        //Create next buttons, disable if on last page
        Button nextBtn = Button.secondary(PaginatorIdUtil.buildButtonId(paginatorId, currentIndex + 1), "Next \u2192");
        Button lastBtn = Button.secondary(PaginatorIdUtil.buildButtonId(paginatorId, PaginatorIndex.LAST.getIndex()), "Last \u21E5");
        if (currentIndex >= totalPages) {
            nextBtn = nextBtn.asDisabled();
            lastBtn = lastBtn.asDisabled();
        }


        return new MessageBuilder(messageEmbed).setActionRows(ActionRow.of(firstBtn, prevBtn, currBtn, nextBtn, lastBtn)).build();
    }


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && PaginatorIdUtil.isValidPaginatorButtonId(buttonId)) {
            event.deferEdit().queue();

            String paginatorId = PaginatorIdUtil.pasrsePaginatorIdFromButtonId(buttonId);
            if (paginatorId != null && paginators.containsKey(paginatorId)) {
                IPaginator paginator = paginators.get(paginatorId);

                int requestedIndex = PaginatorIdUtil.parseIndexFromButtonId(buttonId);
                MessageEmbed embed = paginator.getPageProvider().apply(requestedIndex);
                int totalPages = paginator.getNumberOfTotalPages();


                Message msg = attachPaginator(paginatorId, requestedIndex, totalPages, embed);
                event.getHook().editOriginal(msg).queue();


            } else {
                MessageEmbed me = new EmbedBuilder()
                        .setColor(DiscordUtil.ERROR_COLOR)
                        .setTitle("Paginator error!")
                        .setDescription("Uh-oh, the button was recognized as a paginator, but failed to find the corresponding page.")
                        .build();
                event.getHook().editOriginalEmbeds(me).queue();
            }
        }
    }
}
