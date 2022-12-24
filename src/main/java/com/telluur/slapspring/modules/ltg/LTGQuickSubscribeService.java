package com.telluur.slapspring.modules.ltg;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.ltg.model.LTGGame;
import com.telluur.slapspring.modules.ltg.model.LTGGameRepository;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service that creates and handles quick subscribe buttons/action rows.
 * This service presents an interface to create the buttons, and handles subsequent button interactions.
 * Buttons/action rows still need to be sent manually by other logic.
 */
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
        sendQuickSubscribeWithMessageBuilder(roles, new MessageCreateBuilder());
    }

    public void sendQuickSubscribe(Collection<Role> roles, String message) {
        String content = String.format("%s\r\n", message);
        sendQuickSubscribeWithMessageBuilder(roles, new MessageCreateBuilder().addContent(content));
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
    private void sendQuickSubscribeWithMessageBuilder(Collection<Role> roles, @NonNull MessageCreateBuilder mcBuilder) {
        Optional<ActionRow> opt = createQSActionRowWithRoles(roles);
        if (opt.isPresent()) {
            ActionRow ltgQuickSubActionRow = opt.get();
            List<ItemComponent> components = ltgQuickSubActionRow.getComponents();
            if (components.size() == 1 && components.get(0).getType().equals(Component.Type.STRING_SELECT)) {
                mcBuilder.addContent("Select the Looking-To-Game roles you wish to join from the dropdown.")
                        .setComponents(ltgQuickSubActionRow, ActionRow.of(INFO_BUTTON));
            } else {
                components.add(INFO_BUTTON);
                mcBuilder.addContent("Use the buttons below to join Looking-To-Game roles.")
                        .setComponents(ltgQuickSubActionRow);
            }
            botSession.getLTGTX().sendMessage(mcBuilder.build()).queue();
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
            //We create max 2 LTG buttons.
            List<Button> buttons = ltgGames.stream().map(ltgGame -> Button.primary(
                    String.format("%s%d", QUICK_SUBSCRIBE_PREFIX, ltgGame.getId()),
                    String.format(SUBSCRIBE_ACTION_TEXT, ltgGame.getAbbreviation()))
            ).collect(Collectors.toList());
            return Optional.of(ActionRow.of(buttons));
        } else { //More than 2, but 25 or less roles.
            //We create a dropdown list instead.
            List<SelectOption> options = ltgGames.stream().map(ltgGame -> SelectOption.of(
                    String.format(SUBSCRIBE_ACTION_TEXT, ltgGame.getAbbreviation()),
                    String.valueOf(ltgGame.getId())
            )).toList();
            ActionRow ltgSelect = ActionRow.of(
                    StringSelectMenu.create(QUICK_SUBSCRIBE_MENU_ID)
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
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(QUICK_SUBSCRIBE_PREFIX)) {
            event.deferReply(true).queue();

            String id = buttonId.substring(QUICK_SUBSCRIBE_PREFIX.length()); //Splits off the QUICK_SUBSCRIBE_PREFIX from the following ID
            Role role = botSession.getBoundGuild().getRoleById(id);
            Member member = Objects.requireNonNull(event.getMember()); //Should never be null, as buttons only get posted to guilds.

            if (role != null) {
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
                MessageEmbed me = LTGUtil.joinFailEmbed("Do the roles in your selection still exist?");
                event.getHook().sendMessageEmbeds(me).queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
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
