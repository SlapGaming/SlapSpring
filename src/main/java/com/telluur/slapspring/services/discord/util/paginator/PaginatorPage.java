package com.telluur.slapspring.services.discord.util.paginator;

import com.telluur.slapspring.services.discord.util.DiscordUtil;
import lombok.*;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;

import static com.telluur.slapspring.services.discord.util.paginator.PaginatorButtonUtil.*;


@Getter
@Builder(builderClassName = "Builder", access = AccessLevel.PUBLIC)
public class PaginatorPage {
    private final @NonNull String paginatorId;
    private final @Nullable String data;
    private final int index, totalPages;
    private final @NonNull MessageEmbed messageEmbed;
    @Singular
    private final @Nullable Collection<ActionRow> additionalActionRows;

    /**
     * Attaches paginator buttons to a MessageEmbed for a specific page
     *
     * @return a message containing the embed and ActionRow with first,prev,counter,next,last buttons.
     */
    public Message toDiscordMessage() {
        String safeData = data != null ? data : "_";

        //Create back buttons, disable if on first page
        Button firstBtn = Button.secondary(buildFirstButton(paginatorId, safeData), "\u21E4 First");
        Button prevBtn = Button.secondary(buildButton(paginatorId, safeData, index - 1), "\u2190 Prev");
        if (index == 0) {
            firstBtn = firstBtn.asDisabled();
            prevBtn = prevBtn.asDisabled();
        }

        //Create disabled button indicating the page number
        Button currBtn = Button.primary(DiscordUtil.ALWAYS_DISABLED_BUTTON_ID, String.format("Page %d/%d", index + 1, totalPages)).asDisabled();

        //Create next buttons, disable if on last page
        Button nextBtn = Button.secondary(buildButton(paginatorId, safeData, index + 1), "Next \u2192");
        Button lastBtn = Button.secondary(buildLastButton(paginatorId, safeData), "Last \u21E5");
        if (index >= (totalPages - 1)) {
            nextBtn = nextBtn.asDisabled();
            lastBtn = lastBtn.asDisabled();
        }

        LinkedList<ActionRow> actionRows = new LinkedList<>();
        actionRows.add(ActionRow.of(firstBtn, prevBtn, currBtn, nextBtn, lastBtn));
        if (additionalActionRows != null) {
            actionRows.addAll(additionalActionRows);
        }
        return new MessageBuilder(messageEmbed).setActionRows(actionRows).build();
    }


}
