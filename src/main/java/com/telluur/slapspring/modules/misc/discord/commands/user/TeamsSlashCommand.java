package com.telluur.slapspring.modules.misc.discord.commands.user;

import com.telluur.slapspring.abstractions.discord.commands.ICommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class TeamsSlashCommand extends ListenerAdapter implements ICommand {
    public static final String COMMAND_NAME = "teams";
    public static final String COMMAND_DESCRIPTION = "Create random teams from the users in your voice channel.";
    public static final String OPTION_NUMBER_NAME = "number";
    public static final String OPTION_NUMBER_DESCRIPTION = "Number of teams to generate";
    public static final String OPTION_EXCLUSION_NAME = "exclusion";
    public static final String OPTION_EXCLUSION_DESCRIPTION = "The user you want to exclude";

    private static final OptionData NUMBER_OPTION = new OptionData(OptionType.INTEGER, OPTION_NUMBER_NAME, OPTION_NUMBER_DESCRIPTION, true)
            .setRequiredRange(2, 9);

    /*
    TODO Update to JDA5 beta:
    - Change to correct mentionable type
    - Implement autocomplete to limit to the users in the VC
     */
    private static final List<OptionData> VAR_EXCLUSION_OPTIONS = IntStream.range(1, 25)
            .mapToObj(i -> new OptionData(OptionType.USER, OPTION_EXCLUSION_NAME + i, OPTION_EXCLUSION_DESCRIPTION, false))
            .toList();

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_NAME, COMMAND_DESCRIPTION)
            .addOptions(NUMBER_OPTION)
            .addOptions(VAR_EXCLUSION_OPTIONS)
            .setGuildOnly(true);

    private static final String BUTTON_RESHUFLE_PREFIX = "TEAMS:";

    //Stores live instances so we can respond to shuffle button request.
    private final Map<String, ShufflingMessage> instances = new HashMap<>();

    @NonNull
    @Override
    public CommandData data() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(@NonNull SlashCommandInteractionEvent event) {
        //Get users from calling voice channel
        Member member = Objects.requireNonNull(event.getMember()); //only used in guild
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.reply("You must be in a voice channel to use this command.").setEphemeral(true).queue();
            return;
        }

        List<Member> vcMembers = Objects.requireNonNull(memberVoiceState.getChannel()).getMembers(); //checked above

        List<Member> excludedMentions = IntStream.range(1, 25)
                .mapToObj(i -> event.getOption(OPTION_EXCLUSION_NAME + i, null, OptionMapping::getAsMember))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Member> pool = new ArrayList<>();
        vcMembers.stream()
                .filter(voiceMember -> !voiceMember.getUser().isBot()) //filter out bots
                .filter(voiceMember -> !excludedMentions.contains(voiceMember)) //filter out mentioned users
                .forEach(pool::add); //create a deepcopy

        int numberOfTeams = event.getOption(OPTION_NUMBER_NAME, 0, OptionMapping::getAsInt);

        if (numberOfTeams < 2) {
            event.reply("This is a __teams__ command. No worries though, for people like you we've added this helpful error message: Number of teams should be ≥ 2").setEphemeral(true).queue();
            return;
        } else if (numberOfTeams > 9) {
            event.reply("Number of teams should be ≤ 9").queue();
            return;
        }
        if (numberOfTeams > pool.size()) {
            event.reply("Good job, you're trying to generate more teams than the number of participants available. You must have done well at school.").queue();
            return;
        }

        instances.put(event.getId(), new ShufflingMessage(event.getHook(), numberOfTeams, pool));

        MessageEmbed me = createShuffledTeamsMessage(numberOfTeams, pool);
        MessageCreateData mcd = new MessageCreateBuilder()
                .setEmbeds(me)
                .setComponents(ActionRow.of(Button.primary(BUTTON_RESHUFLE_PREFIX + event.getId(), "Shuffle")))
                .build();

        //Send teams message, and schedule shuffle button disable and instance removal from memory
        event.reply(mcd).submit()
                .thenCompose(interactionHook ->
                        interactionHook.editOriginalComponents(
                                ActionRow.of(Button.primary(BUTTON_RESHUFLE_PREFIX + event.getId(), "Shuffle").asDisabled())
                        ).submitAfter(30, TimeUnit.SECONDS))
                .thenAccept(ok -> instances.remove(event.getId()));
    }


    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId != null && buttonId.startsWith(BUTTON_RESHUFLE_PREFIX)) {
            String interactionHookId = buttonId.substring(BUTTON_RESHUFLE_PREFIX.length());
            if (instances.containsKey(interactionHookId)) {
                ShufflingMessage shufflingMessage = instances.get(interactionHookId);
                MessageEmbed me = createShuffledTeamsMessage(shufflingMessage.getNumberOfTeams(), shufflingMessage.getPool());
                shufflingMessage.getInteractionHook().editOriginalEmbeds(me).queue();
                event.reply("Shuffled teams...").setEphemeral(true).queue();
            } else {
                event.reply("Uh-oh, we couldn't find the instance for this button.").setEphemeral(true).queue();
            }
        }
    }

    private @NonNull MessageEmbed createShuffledTeamsMessage(int numberOfTeams, List<Member> pool) {
        //Create the team objects
        List<List<String>> teams = new LinkedList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            teams.add(new LinkedList<>());
        }

        //Randomize the teams by shuffling the pool, and then filling the teams.
        Collections.shuffle(pool);
        int assign = 0;
        for (Member teamMate : pool) {
            teams.get(assign).add(teamMate.getEffectiveName());
            assign = (assign + 1) % numberOfTeams;
        }

        //Create the embedded message reply
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random teams")
                .setColor(Color.GREEN);

        int teamNo = 1;
        for (List<String> team : teams) {
            eb.addField("Team " + teamNo++, String.join("\r\n", team), true);
        }

        return eb.build();
    }

    @AllArgsConstructor
    @Getter
    private static class ShufflingMessage {
        private InteractionHook interactionHook; //Valid for max 15 minutes
        private int numberOfTeams;
        private List<Member> pool;
    }


}
