package com.telluur.slapspring.services.discord.impl.ltg;

import com.telluur.slapspring.model.ltg.LTGGame;
import com.telluur.slapspring.model.ltg.LTGGameRepository;
import com.telluur.slapspring.services.discord.BotSession;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
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

    private static final String QUICK_SUBSCRIBE_PREFIX = LTGUtil.LTG_INTERACTABLE_PREFIX + "QS:";
    private static final String QUICK_SUBSCRIBE_MENU_ID = LTGUtil.LTG_INTERACTABLE_PREFIX + "QS:MENU";

    private static final Button INFO_BUTTON = Button.link("https://discord.com/channels/276858200853184522/596030791214170112/663754415504752650", "What's this?");

    private static final String SUBSCRIBE_ACTION_TEXT = "Subscribe to %s";

    @Autowired
    private BotSession botSession;

    @Autowired
    private LTGGameRepository repository;

    @Autowired
    private LTGRoleService ltgRoleService;


    public void sendQuickSubscribe(Collection<Role> roles) {
        sendQuickSubscribeWithMessageBuilder(roles, new MessageBuilder());
    }

    public void sendQuickSubscribeWithMessage(Collection<Role> roles, String message) {
        sendQuickSubscribeWithMessageBuilder(roles, new MessageBuilder().appendFormat("%s\r\n", message));
    }


    /**
     * Sends a message to LTG tx with buttons corresponding to the roles provided.
     * The buttons presses trigger the bot to add the role to the pressing user, this is handled in onButtonClick()
     * <p>
     * When 1 0r 2 valid LTG roles are supplied, two buttons are created,
     * When more than 2 valid LTG roles are supplied, we create a dropdown menu instead.
     *
     * @param roles the Roles the user will be able to join
     */
    private void sendQuickSubscribeWithMessageBuilder(Collection<Role> roles, @Nonnull MessageBuilder msgBuilder) {

        Optional<ActionRow> opt = createQSActionRowWithRoles(roles);
        if (opt.isPresent()) {
            ActionRow ltgQuickSubActionRow = opt.get();
            if (ltgQuickSubActionRow.getType().equals(Component.Type.SELECT_MENU)) {
                msgBuilder.append("Select the Looking-To-Game roles you wish to join from the dropdown.")
                        .setActionRows(ltgQuickSubActionRow, ActionRow.of(INFO_BUTTON))
                        .build();
            } else {
                msgBuilder.append("Use the buttons below to join Looking-To-Game roles.")
                        .setActionRows(ltgQuickSubActionRow)
                        .build();
            }
            botSession.getLTGTX().sendMessage(msgBuilder.build()).queue();
        }
    }

    /**
     * Creates the ActionRows for the quick subscribe buttons
     * The buttons presses trigger the bot to add the corresponding role to the pressing user, this is handled in onButtonClick()
     * <p>
     * When 1 0r 2 valid LTG roles are supplied, two buttons are created,
     * When more than 2 valid LTG roles are supplied, we create a dropdown menu instead.
     *
     * @param ltgGames the ltgGames the user will be able to join. This does not check whether the corresponding discord role still exists.
     * @return Optional ActionRow, might return when empty when no valid LTG roles were provided, or more than 25 LTG roles.
     */
    public Optional<ActionRow> createQSActionRowWithLTGGames(Collection<LTGGame> ltgGames) {
        if (ltgGames == null || ltgGames.size() <= 0 || ltgGames.size() > 25) {
            return Optional.empty();
        } else if (ltgGames.size() == 1 || ltgGames.size() == 2) {
            //We create 2 LTG buttons and the info button.
            List<Button> buttons = ltgGames.stream().map(ltgGame -> Button.primary(
                    String.format("%s%d", QUICK_SUBSCRIBE_PREFIX, ltgGame.getId()),
                    String.format(SUBSCRIBE_ACTION_TEXT, ltgGame.getAbbreviation()))
            ).collect(Collectors.toList());
            buttons.add(INFO_BUTTON);
            return Optional.of(ActionRow.of(buttons));
        } else { //More than 2, but 25 or less roles.
            //We create a dropdown list instead.
            List<SelectOption> options = ltgGames.stream().map(ltgGame -> SelectOption.of(
                    String.format(SUBSCRIBE_ACTION_TEXT, ltgGame.getAbbreviation()),
                    String.valueOf(ltgGame.getId())
            )).toList();
            ActionRow ltgSelect = ActionRow.of(SelectMenu.create(QUICK_SUBSCRIBE_MENU_ID)
                    .addOptions(options)
                    .setMaxValues(25)
                    .build());
            return Optional.of(ltgSelect);
        }
    }

    /**
     * Creates the ActionRows for the quick subscribe buttons
     * The buttons presses trigger the bot to add the role to the pressing user, this is handled in onButtonClick()
     * <p>
     * When 1 0r 2 valid LTG roles are supplied, two buttons are created,
     * When more than 2 valid LTG roles are supplied, we create a dropdown menu instead.
     *
     * @param roles the discord roles the user will be able to join. Non-LTG roles are filtered.
     * @return Optional ActionRow, might return when empty when no valid LTG roles were provided, or more than 25 LTG roles.
     */
    public Optional<ActionRow> createQSActionRowWithRoles(Collection<Role> roles) {
        if (roles == null) {
            return Optional.empty();
        } else {
            List<LTGGame> ltgGames = roles.stream()
                    .distinct() //We only want unique roles.
                    .map(role -> repository.findById(role.getIdLong()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            return createQSActionRowWithLTGGames(ltgGames);
        }
    }


    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(QUICK_SUBSCRIBE_PREFIX)) {
            event.deferReply(true).queue();

            String id = buttonId.substring(QUICK_SUBSCRIBE_PREFIX.length()); //Splits off the QUICK_SUBSCRIBE_PREFIX from the following ID
            Role role = botSession.getBoundGuild().getRoleById(id);
            Member member = event.getMember();

            if (member != null && role != null) {
                ltgRoleService.addMemberToRoleIfLTG(member, role,
                        r -> {
                            MessageEmbed me = LTGUtil.joinSuccessEmbed(r);
                            event.getHook().sendMessageEmbeds(me).queue();
                            botSession.getLTGTX().sendMessage(LTGUtil.joinBroadcastMessage(member, r)).queue();
                        },
                        fail -> {
                            MessageEmbed me = LTGUtil.joinFailEmbed(fail.getMessage());
                            event.getHook().sendMessageEmbeds(me).queue();
                        });
            } else {
                //member should never be null, as the buttons only get posted in the guild.
                event.getHook().sendMessage("Uh-oh, something went wrong...").queue();
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        event.deferReply(true).queue();

        if (Objects.requireNonNull(event.getComponent().getId()).equals(QUICK_SUBSCRIBE_MENU_ID)) {
            //ID should never be null...
            Guild guild = botSession.getBoundGuild();

            List<Role> selectedGuildRoles = event.getValues().stream()
                    .map(guild::getRoleById)
                    .filter(Objects::nonNull)
                    .toList();

            if (selectedGuildRoles.size() >= 1) {
                Member member = Objects.requireNonNull(event.getMember());//Should never be null as it is in guild.

                ltgRoleService.addMemberToRolesIfLTG(member, selectedGuildRoles,
                        joinedRoles -> {
                            MessageEmbed me = LTGUtil.joinSuccessEmbed(joinedRoles);
                            event.getHook().sendMessageEmbeds(me).queue();
                            botSession.getLTGTX().sendMessage(LTGUtil.joinBroadcastMessage(member, joinedRoles)).queue();
                        },
                        fail -> {
                            MessageEmbed me = LTGUtil.joinFailEmbed(fail.getMessage());
                            event.getHook().sendMessageEmbeds(me).queue();
                        });
            } else {
                MessageEmbed me = LTGUtil.joinFailEmbed("Do the roles in your selection still exist?");
                event.getHook().sendMessageEmbeds(me).queue();
            }
        }
    }
}
