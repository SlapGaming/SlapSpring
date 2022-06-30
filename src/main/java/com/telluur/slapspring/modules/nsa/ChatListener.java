package com.telluur.slapspring.modules.nsa;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachment;
import com.telluur.slapspring.modules.nsa.model.LoggedMessage;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


@Service
public class ChatListener extends ListenerAdapter {
    private static final Color NSA_COLOR = new Color(1, 1, 222);

    private static final String LINE = "\u2015";


    private final Map<String, Message> db = new HashMap<>();

    @Autowired
    LoggedMessageRepository messageRepository;

    @Autowired
    private BotSession session;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //Skip bot messages, and non-focussed channels
        if (event.getAuthor().isBot() || !inFocusChannel(event)) {
            return;
        }

        Message message = event.getMessage();
        List<CompletableFuture<LoggedAttachment>> attachmentFutures = message.getAttachments().stream()
                .map(attachment -> {
                            CompletableFuture<InputStream> download = attachment.getProxy().download();
                            return download.thenApply(inputStream -> {
                                try (inputStream) {
                                    LoggedAttachment la = new LoggedAttachment();

                                    byte[] content = inputStream.readAllBytes();
                                    la.setContent(content);

                                    la.setId(attachment.getIdLong());
                                    la.setName(attachment.getFileName());
                                    la.setContentType(attachment.getContentType());

                                    return la;
                                } catch (IOException e) {
                                    return null;
                                }
                            });
                        }
                ).toList();
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                attachmentFutures.toArray(new CompletableFuture[0])
        );
        allFutures.thenAccept(v -> {
            List<LoggedAttachment> loggedAttachments = attachmentFutures.stream().map(CompletableFuture::join).toList();
            LoggedMessage loggedMessage = new LoggedMessage();
            loggedMessage.setId(message.getIdLong());
            loggedMessage.setChannelId(message.getChannel().getIdLong());
            loggedMessage.setUserId(message.getAuthor().getIdLong());
            loggedMessage.setJumpUrl(message.getJumpUrl());
            loggedMessage.setContentRaw(message.getContentRaw());
            loggedMessage.setAttachmentList(loggedAttachments);
            messageRepository.save(loggedMessage);
        });

    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (inFocusChannel(event) && messageRepository.existsById(event.getMessageIdLong())) {
            Message oldMsg = db.get(event.getMessageId());
            Message updatedMsg = event.getMessage();

            String channelTypeString = switch (event.getChannelType()) {
                case TEXT -> "Text Channel";
                case VOICE -> "Voice Channel";
                case GUILD_PUBLIC_THREAD -> "Public Thread";
                case GUILD_PRIVATE_THREAD -> "Private Thread";
                default -> "Unknown Channel Type";
            };


            String description = String.format("""
                            **%s:** %s
                            **Member:** %s (%s#%s)
                                                
                            **\u2015\u2015\u2015\u2015\u2015 \u25BCOLD\u25BC \u2015\u2015\u2015\u2015\u2015**
                                                
                            %s
                                                
                            **\u2015\u2015 \u25B2OLD\u25B2 \u2015 \u25BCNEW\u25BC \u2015\u2015**
                                                        
                            %s
                                                        
                             **\u2015\u2015\u2015\u2015\u2015 \u25B2NEW\u25B2 \u2015\u2015\u2015\u2015\u2015**
                            """,
                    channelTypeString,
                    event.getTextChannel().getName(),
                    Objects.requireNonNull(event.getMember()).getAsMention(), //Checked by event.isFromGuild()
                    event.getAuthor().getName(),
                    event.getAuthor().getDiscriminator(),
                    oldMsg.getContentDisplay(),
                    updatedMsg.getContentDisplay()
            );



            /*
            TODO attachments

            Move messages to JPA, including blobbed attachments.
             */


            MessageEmbed me = new EmbedBuilder()
                    .setColor(NSA_COLOR)
                    .setTitle("NSA: A message was edited")
                    .setDescription(description)
                    .build();

            session.getNSATX().sendMessageEmbeds(me).queue();

            db.replace(event.getMessageId(), updatedMsg);
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        super.onMessageDelete(event);
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        super.onMessageBulkDelete(event);
    }

    /**
     * We only want to log messages from the bound guild, and exclude the NSA channel to avoid echo
     *
     * @param event the event to perform the check on
     * @return true when it is a target channel for logging
     */
    private boolean inFocusChannel(@NotNull GenericMessageEvent event) {
        return event.isFromGuild()
                && event.getGuild().equals(session.getBoundGuild())
                && !event.getChannel().equals(session.getNSATX());
    }
}
