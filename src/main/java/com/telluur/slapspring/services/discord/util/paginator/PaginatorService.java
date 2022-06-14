package com.telluur.slapspring.services.discord.util.paginator;


import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

/**
 * Single listener implementation for a generic paginator service.
 * The IDs set on the discord buttons pass persistent state on the requested paginator, and the requested page.
 * Paginator ID stucture: "PAGINATOR:[PAGINATOR_PREFIX]:[REQUESTED_PAGE_INT]"
 */
public class PaginatorService extends ListenerAdapter {
    private static final String PAGINATOR_PREFIX = "PAGINATOR:";


    private final HashMap<String, IPaginator> paginators = new HashMap<>();


    public PaginatorService(@Autowired List<IPaginator> iPaginators) {
        for (IPaginator iPaginator : iPaginators) {
            paginators.put(iPaginator.getPaginatorButtonPrefix(), iPaginator);
        }
    }

    @AllArgsConstructor
    private enum PAGINATOR_INDEX {
        /*
        Simplify arithmetic on paginator buttons.
        Page index range: [0,)
        Previous button index range [-1,)
        Hard coded values for first (-2) and last (-3)
         */
        FIRST(-2),
        LAST(-3);
        private final int index;
    }


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(PAGINATOR_PREFIX)) {
            event.deferEdit().queue();

            String paginatorString = buttonId.substring(PAGINATOR_PREFIX.length()); //Splits off the PAGINATOR_PREFIX from the following index

            //TODO split on paginator id and index int





            /*
            Message msg;
            try {
                int index = Integer.parseInt(indexString);
                msg = createListMessage(index);


            } catch (NumberFormatException e) {
                msg = new MessageBuilder(LTGUtil.failureEmbed("Uh-oh, the paginator broke :(")).build();
            }
            event.getHook().editOriginal(msg).queue();

            */
        }
    }
}
