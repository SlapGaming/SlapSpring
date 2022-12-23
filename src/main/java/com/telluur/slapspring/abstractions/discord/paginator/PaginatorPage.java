package com.telluur.slapspring.abstractions.discord.paginator;

import com.telluur.slapspring.util.discord.DiscordUtil;
import lombok.*;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


@Getter
@Builder(builderClassName = "Builder", access = AccessLevel.PUBLIC)
public class PaginatorPage {
    private final @NonNull String paginatorId;
    private final String data; //Nullable
    private final int index, totalPages;
    private final @NonNull MessageEmbed messageEmbed;
    @Singular
    private final Collection<ActionRow> additionalActionRows; // Might be null

    /**
     * Creates a new paginator message, including the embeds, buttons, and additional actionrows.
     *
     * @return paginator CreateData
     */
    public MessageCreateData toMessageCreateData() {
        List<ActionRow> actionRows = createPaginatorActionRow();
        return new MessageCreateBuilder()
                .setEmbeds(messageEmbed)
                .setComponents(actionRows)
                .build();
    }

    /**
     * Creates an edited paginator message, including the embeds, buttons, and additional actionrows.
     *
     * @return paginator EditData
     */
    public MessageEditData toMessageEditData() {
        List<ActionRow> actionRows = createPaginatorActionRow();
        return new MessageEditBuilder()
                .setEmbeds(messageEmbed)
                .setComponents(actionRows)
                .build();
    }

    /**
     * Computes paginator buttons for this paginator page.
     *
     * @return ActionRows containing paginator buttons and addiotionalActionRows if exists.
     */
    private List<ActionRow> createPaginatorActionRow() {
        String safeData = data != null ? data : "_";

        //Create back buttons, disable if on first page
        Button firstBtn = Button.secondary(PaginatorButtonUtil.buildFirstButton(paginatorId, safeData), "\u21E4 First");
        Button prevBtn = Button.secondary(PaginatorButtonUtil.buildButton(paginatorId, safeData, index - 1), "\u2190 Prev");
        if (index == 0) {
            firstBtn = firstBtn.asDisabled();
            prevBtn = prevBtn.asDisabled();
        }

        //Create disabled button indicating the page number
        Button currBtn = Button.primary(DiscordUtil.ALWAYS_DISABLED_BUTTON_ID, String.format("Page %d/%d", index + 1, totalPages)).asDisabled();

        //Create next buttons, disable if on last page
        Button nextBtn = Button.secondary(PaginatorButtonUtil.buildButton(paginatorId, safeData, index + 1), "Next \u2192");
        Button lastBtn = Button.secondary(PaginatorButtonUtil.buildLastButton(paginatorId, safeData), "Last \u21E5");
        if (index >= (totalPages - 1)) {
            nextBtn = nextBtn.asDisabled();
            lastBtn = lastBtn.asDisabled();
        }

        LinkedList<ActionRow> actionRows = new LinkedList<>();
        actionRows.add(ActionRow.of(firstBtn, prevBtn, currBtn, nextBtn, lastBtn));
        if (additionalActionRows != null) {
            actionRows.addAll(additionalActionRows);
        }
        return actionRows;
    }


}
