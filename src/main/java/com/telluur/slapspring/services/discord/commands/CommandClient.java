package com.telluur.slapspring.services.discord.commands;

import com.telluur.slapspring.services.discord.BotSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/***
 * CommandClient for managing and using of Discord Slash Commands
 *
 * Upserts all commands implementing the ICommand interface
 * Implements a single Listener for all commands, routing to the implementing command classes.
 */
@Component
@Slf4j
public class CommandClient extends ListenerAdapter {
    private final HashMap<String, ICommand> commands = new HashMap<>();


    /***
     * Created the commandClient object, by upserting all commands, and registering it to the command router.
     * @param botSession Commands are upserted to the JDA instance in this session.
     * @param commandList A List of commands implementing the ICommand interface.
     */
    public CommandClient(@Autowired BotSession botSession, @Autowired List<ICommand> commandList) {
        log.info("Found commands: {}", commandList.stream()
                .map(ic -> ic.data().getName())
                .toList());


        Guild boundGuild = botSession.getBoundGuild();

        // Upsert all command data (@Components implementing ICommand) with discord to the bound guild.
        List<CommandData> cds = commandList.stream().map(ICommand::data).toList();
        boundGuild.updateCommands().addCommands(cds).queue(
                ok -> {
                },
                throwable -> log.error("Failed to upsert commands to discord", throwable)
        );

        //Register to command listener
        commandList.forEach(iCommand -> {
            CommandData commandData = iCommand.data();
            String name = commandData.getName();
            commands.put(name, iCommand);
        });
    }


    /***
     * Routes te command to the registerd implementing ICommand class.
     * @param event The InteractionEvent for the command.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();
        if (commands.containsKey(name)) {
            commands.get(name).handle(event);
        } else {
            log.warn("No handler found for command '{}'", name);
        }
    }
}