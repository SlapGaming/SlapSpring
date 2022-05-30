package com.telluur.slapspring.services.discord.impl.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LTGQuickSubscribeService extends ListenerAdapter {
    private static final String QUICK_SUBSCRIBE_MENU_ID = "menu:qs";
    private static final String QUICK_SUBSCRIBE_PREFIX = "qs:";
    private static final Button INFO_BUTTON = Button.link("https://discord.com/channels/276858200853184522/596030791214170112/663754415504752650", "What's this?");

    @Autowired
    private BotSession botSession;

    @Autowired
    private LTGGameRepository repository;

    @Autowired
    private LTGRoleService ltgRoleService;


    /**
     * Sends a message to tx with buttons corresponding to the roles provided.
     * The buttons presses trigger the bot to add the role to the pressing user, this is handled in onButtonClick()
     * <p>
     * When 1 0r 2 valid LTG roles are supplied, two buttons are created,
     * When more than 2 valid LTG roles are supplied, we create a dropdown menu instead.
     *
     * @param roles the Roles the user will be able to join
     */
    public void sendQuicksubscribeMessage(Collection<Role> roles) {
        List<LTGGame> ltgGames = roles.stream()
                .distinct() //We only want unique roles.
                .map(role -> repository.findById(role.getIdLong()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();


        MessageBuilder msgBuilder = new MessageBuilder();
        if (ltgGames.size() <= 2) {
            //We create 2 LTG buttons and the info button.
            List<Button> buttons = ltgGames.stream().map(ltgGame -> Button.primary(
                    String.format("%s%d", QUICK_SUBSCRIBE_PREFIX, ltgGame.getId()),
                    String.format("Subscribe to %s", ltgGame.getAbbreviation()))
            ).collect(Collectors.toList());
            buttons.add(INFO_BUTTON);
            msgBuilder.append("Hit the buttons below to join LTG roles.")
                    .setActionRows(ActionRow.of(buttons));
        } else { //25 is the max number of options in a select menu
            //We create a dropdown list instead.
            List<SelectOption> options = ltgGames.stream().map(ltgGame -> SelectOption.of(
                    String.format("Subscribe to %s", ltgGame.getAbbreviation()),
                    String.valueOf(ltgGame.getId())
            )).toList();
            ActionRow ltgSelect = ActionRow.of(SelectMenu.create(QUICK_SUBSCRIBE_MENU_ID)
                    .addOptions(options)
                    .setMaxValues(25)
                    .build());

            msgBuilder.append("Select the LTG roles you wish to join.")
                    .setActionRows(ltgSelect, ActionRow.of(INFO_BUTTON));
        }
        botSession.getLTGTX().sendMessage(msgBuilder.build()).queue();
    }


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(QUICK_SUBSCRIBE_PREFIX)) {
            String id = buttonId.substring(3); //Splits off the QUICK_SUBSCRIBE_PREFIX from the following ID
            Role role = botSession.getBoundGuild().getRoleById(id);
            Member member = event.getMember();

            if (member != null && role != null) {
                ltgRoleService.addMemberToRoleIfLTG(member, role,
                        r -> {
                            event.getHook().sendMessageFormat("Successfully joined ").queue();
                            //TODO Implement
                        },
                        fail -> {
                            //TODO Implement
                        });
            } else {
                event.getHook().sendMessage("Uh-oh, something went wrong...").queue();
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (event.getComponent().getId().equals(QUICK_SUBSCRIBE_MENU_ID)) {
            Guild guild = botSession.getBoundGuild();

            List<Role> roles = event.getValues().stream()
                    .map(guild::getRoleById)
                    .filter(Objects::nonNull)
                    .toList();


            //TODO Implement
        }

    }


}
