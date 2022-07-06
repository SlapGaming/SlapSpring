package com.telluur.slapspring.modules.nsa;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachment;
import com.telluur.slapspring.modules.nsa.model.LoggedMessage;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageContent;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Service
public class ChatListener extends ListenerAdapter {
    private static final Color NSA_COLOR = new Color(1, 1, 222);

    private static final String LINE = "\u2015";

    private static final int CHAR_LIMIT = 1500;


    private final Map<String, Message> db = new HashMap<>();

    @Autowired
    LoggedMessageRepository messageRepository;

    @Autowired
    private BotSession session;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.println("MessageReceivedEvent");
        //Skip bot messages, and non-focussed channels
        if (event.getAuthor().isBot() || !inFocusChannel(event)) {
            return;
        }

        System.out.println("Saving message");

        Message message = event.getMessage();
        List<CompletableFuture<LoggedAttachment>> attachmentFutures = message.getAttachments().stream()
                .map(attachment -> {
                            CompletableFuture<InputStream> download = attachment.getProxy().download();
                            return download.thenApply(inputStream -> {
                                try (inputStream) {
                                    LoggedAttachment la = new LoggedAttachment();
                                    la.setId(attachment.getIdLong());

                                    byte[] content = inputStream.readAllBytes();
                                    la.setContent(content);

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
            LoggedMessage loggedMessage = new LoggedMessage();
            loggedMessage.setId(message.getIdLong());
            loggedMessage.setChannelId(message.getChannel().getIdLong());
            loggedMessage.setUserId(message.getAuthor().getIdLong());
            loggedMessage.setJumpUrl(message.getJumpUrl());
            loggedMessage.appendContentHistory(message.getContentRaw());

            List<LoggedAttachment> loggedAttachments = attachmentFutures.stream()
                    .map(CompletableFuture::join)
                    .peek(la -> la.setLoggedMessage(loggedMessage)) //Set two way reference
                    .toList();


            loggedMessage.setAttachmentList(loggedAttachments);
            messageRepository.save(loggedMessage);
        });

    }

    @Transactional
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        System.out.println("MessageUpdateEvent");
        long msgId = event.getMessageIdLong();
        Optional<LoggedMessage> optionalLoggedMessage = messageRepository.findById(msgId);

        if (inFocusChannel(event) && optionalLoggedMessage.isPresent()) {
            System.out.println("Updating a logged message");
            /*
             * An update might be fired for multiple reasons, here we filter 2, both are always fired separately:
             * - text content of a message has changed
             * - One or more attachments have been deleted from a message that contains text or multiple attachments
             * (Attachment deletion for messages containing only a single attachment and no text are fired in onMessageDelete())
             */

            LoggedMessage loggedMessage = optionalLoggedMessage.get();
            Message eventMessage = event.getMessage();

            String channelTypeString = switch (event.getChannelType()) {
                case TEXT -> "Text Channel";
                case VOICE -> "Voice Channel";
                case GUILD_PUBLIC_THREAD -> "Public Thread";
                case GUILD_PRIVATE_THREAD -> "Private Thread";
                default -> "Unknown Channel Type";
            };


            List<LoggedMessageContent> contentHistory = loggedMessage.getContentHistory();
            String oldRaw = contentHistory.get(contentHistory.size() - 1).getContentRaw();
            String newRaw = eventMessage.getContentRaw();
            if (!oldRaw.equals(newRaw)) {
                //text content of a message has changed

                String description = String.format("""
                                **%s:** %s
                                **Member:** %s (%s#%s)
                                **Edit number:** %d
                                                    
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
                        contentHistory.size(),
                        oldRaw.length() <= CHAR_LIMIT ? oldRaw : oldRaw.substring(0, CHAR_LIMIT) + "...",
                        newRaw.length() <= CHAR_LIMIT ? newRaw : newRaw.substring(0, CHAR_LIMIT) + "..."
                );
                MessageEmbed me = new EmbedBuilder()
                        .setColor(NSA_COLOR)
                        .setTitle("NSA: A message was edited")
                        .setDescription(description)
                        .build();


                session.getNSATX().sendMessageEmbeds(me).queue();

                loggedMessage.appendContentHistory(eventMessage.getContentRaw());
                messageRepository.save(loggedMessage);


            }


            List<LoggedAttachment> loggedLiveAttachments = loggedMessage.getAttachmentList()
                    .stream()
                    .filter(la -> !la.isDeleted())
                    .toList();
            List<Message.Attachment> eventAttachments = eventMessage.getAttachments();
            if (loggedLiveAttachments.size() > eventAttachments.size()) {
                System.out.println("Attachment deletion");
                /*
                 * A single (!) attachment has been deleted from a message that contains text or multiple attachments
                 * Client forces confirmation for deletion on single attachment. Assumption: Unlikely that a single update would fire for multiple.
                 * (Attachment deletion for messages containing only a single attachment and no text are fired in onMessageDelete())
                 */
                List<Long> eventAttachmentIds = eventAttachments.stream()
                        .map(Message.Attachment::getIdLong)
                        .toList();

                List<LoggedAttachment> deletedAttachments = loggedLiveAttachments.stream()
                        .filter(la -> !eventAttachmentIds.contains(la.getId()))
                        .toList();

                //TODO this only souts the deleted attachment. Create embed and mark deleted.
                //TODO create http endpoint for displaying the attachments.
                deletedAttachments.forEach(a -> System.out.println(a.getName()));

            }
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        System.out.println("MessageDeleteEvent");
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
