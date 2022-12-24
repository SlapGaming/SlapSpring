package com.telluur.slapspring.modules.nsa.listeners;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.nsa.NSAUtil;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachment;
import com.telluur.slapspring.modules.nsa.model.LoggedMessage;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageContent;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.telluur.slapspring.util.discord.DiscordUtil.channelTypeToString;

/**
 * Logging service that logs full message history to DB, and notifies admins in the NSA channel
 * TODO implement EmojiReaction logging
 */
@Slf4j
@Service
public class ChatListener extends ListenerAdapter {

    private static final int CHAR_LIMIT = 1500;

    @Autowired
    private LoggedMessageRepository messageRepository;

    @Autowired
    private BotSession session;

    @Autowired
    private String baseUrl;

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        //Skip bot messages, and non-focussed channels
        if (event.getAuthor().isBot() || !inFocusChannel(event)) {
            return;
        }

        Message message = event.getMessage();
        List<CompletableFuture<LoggedAttachment>> attachmentFutures = message.getAttachments()
                .stream()
                .map(attachment -> attachment.getProxy()
                        .download()
                        .thenApply(inputStream -> {
                            try {
                                LoggedAttachment la = new LoggedAttachment();
                                la.setId(attachment.getIdLong());

                                byte[] content = inputStream.readAllBytes();
                                la.setContent(content);

                                la.setName(attachment.getFileName());

                                la.setContentType(attachment.getContentType());
                                return la;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).handle((loggedAttachment, throwable) -> {
                            if (throwable != null) {
                                log.warn("Failed to download attachment {}", attachment.getProxyUrl(), throwable);
                                return null;
                            } else {
                                return loggedAttachment;
                            }
                        })
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

            Message refMsg = message.getReferencedMessage();
            if (refMsg != null) {
                loggedMessage.setReferencedMessageId(refMsg.getIdLong());
            }

            loggedMessage.appendContentHistory(message.getContentRaw());

            List<LoggedAttachment> loggedAttachments = attachmentFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .peek(la -> la.setLoggedMessage(loggedMessage)) //Set two way reference
                    .toList(); //note: preceding peek() needs fully evaluating terminal in stream

            loggedMessage.setAttachmentList(loggedAttachments);
            messageRepository.save(loggedMessage);
        });
    }

    @Transactional
    @Override
    public void onMessageUpdate(@NonNull MessageUpdateEvent event) {
        //Skip bot messages, and non-focussed channels
        if (event.getAuthor().isBot() || !inFocusChannel(event)) {
            return;
        }

        long msgId = event.getMessageIdLong();
        Optional<LoggedMessage> optionalLoggedMessage = messageRepository.findById(msgId);

        if (optionalLoggedMessage.isPresent()) {
            /*
             * An update might be fired for multiple reasons, here we filter 2, both are always fired separately:
             * - text content of a message has changed
             * - One or more attachments have been deleted from a message that contains text or multiple attachments
             * (Attachment deletion for messages containing only a single attachment and no text are fired in onMessageDelete())
             */

            LoggedMessage loggedMessage = optionalLoggedMessage.get();
            Message eventMessage = event.getMessage();

            List<LoggedMessageContent> contentHistory = loggedMessage.getContentHistory();
            String oldRaw = contentHistory.get(contentHistory.size() - 1).getContentRaw();
            String newRaw = eventMessage.getContentRaw();
            if (!oldRaw.equals(newRaw)) {
                //text content of a message has changed

                String description = String.format("""
                                **%s:** %s
                                **Member:** %s (%s)
                                **Edit number:** %d
                                                    
                                **\u2015\u2015\u2015\u2015\u2015 \u25BCOLD\u25BC \u2015\u2015\u2015\u2015\u2015**
                                                    
                                %s
                                                    
                                **\u2015\u2015 \u25B2OLD\u25B2 \u2015 \u25BCNEW\u25BC \u2015\u2015**
                                                            
                                %s
                                                            
                                 **\u2015\u2015\u2015\u2015\u2015 \u25B2NEW\u25B2 \u2015\u2015\u2015\u2015\u2015**
                                """,
                        channelTypeToString(event.getChannelType()),
                        event.getChannel().getName(),
                        Objects.requireNonNull(event.getMember()).getAsMention(), //Checked by event.isFromGuild()
                        event.getAuthor().getAsTag(),
                        contentHistory.size(),
                        oldRaw.length() <= CHAR_LIMIT ? oldRaw : oldRaw.substring(0, CHAR_LIMIT) + "...",
                        newRaw.length() <= CHAR_LIMIT ? newRaw : newRaw.substring(0, CHAR_LIMIT) + "..."
                );
                MessageEmbed me = new EmbedBuilder()
                        .setColor(NSAUtil.NSA_EDIT_COLOR)
                        .setTitle("A message was edited")
                        .setDescription(description)
                        .build();
                MessageCreateData mcd = new MessageCreateBuilder()
                        .setEmbeds(me)
                        .setComponents(createActionRowWithArchiveButton(msgId))
                        .build();
                session.getNSATX().sendMessage(mcd).queue();

                loggedMessage.appendContentHistory(eventMessage.getContentRaw());
                messageRepository.save(loggedMessage);
            }


            List<LoggedAttachment> loggedLiveAttachments = loggedMessage.getAttachmentList()
                    .stream()
                    .filter(la -> !la.isDeleted())
                    .toList();
            List<Message.Attachment> eventAttachments = eventMessage.getAttachments();
            if (loggedLiveAttachments.size() > eventAttachments.size()) {
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

                deletedAttachments.forEach(la -> {
                    la.setDeleted(); //Saved later

                    String attUrl = attachmentUrl(la);

                    String description = String.format("""
                                    **%s:** %s
                                    **Member:** %s (%s)
                                                                        
                                    **File name:** %s
                                    **Content Type:** %s
                                    **NSA Link:** %s
                                    """,
                            channelTypeToString(event.getChannelType()),
                            event.getChannel().getName(),
                            Objects.requireNonNull(event.getMember()).getAsMention(), //Checked by event.isFromGuild()
                            event.getAuthor().getAsTag(),
                            la.getName(),
                            la.getContentType(),
                            attUrl);

                    sendAttachmentDeletionLog(msgId, la, description);
                });
                messageRepository.save(loggedMessage);
            }
        }
    }

    @Transactional
    @Override
    public void onMessageDelete(@NonNull MessageDeleteEvent event) {
        //Skip non-focussed channels
        if (!inFocusChannel(event)) {
            return;
        }

        long msgId = event.getMessageIdLong();
        Optional<LoggedMessage> optionalLoggedMessage = messageRepository.findById(msgId);

        if (optionalLoggedMessage.isPresent()) {

            LoggedMessage loggedMessage = optionalLoggedMessage.get();

            Guild guild = session.getBoundGuild();
            Member member = guild.getMemberById(loggedMessage.getUserId());
            String memberMention = member != null ? member.getAsMention() : "<NON SLAP>";

            User user = session.getJda().getUserById(loggedMessage.getUserId());
            String userMention = user != null ? user.getAsTag() : "<UNKNOWN>";


            List<LoggedMessageContent> contentHistory = loggedMessage.getContentHistory();
            if (contentHistory.size() > 0) {
                String contentRaw = contentHistory.get(contentHistory.size() - 1).getContentRaw();

                String description = String.format("""
                                **%s:** %s
                                **Member:** %s (%s)
                                                    
                                **\u2015\u2015\u2015\u2015\u2015 \u25BCOLD\u25BC \u2015\u2015\u2015\u2015\u2015**
                                                            
                                %s
                                                            
                                 **\u2015\u2015\u2015\u2015\u2015 \u25B2OLD\u25B2 \u2015\u2015\u2015\u2015\u2015**
                                """,
                        channelTypeToString(event.getChannelType()),
                        event.getChannel().getName(),
                        memberMention,
                        userMention,
                        contentRaw.length() <= CHAR_LIMIT ? contentRaw : contentRaw.substring(0, CHAR_LIMIT) + "...");

                MessageEmbed me = new EmbedBuilder()
                        .setColor(NSAUtil.NSA_DELETION_COLOR)
                        .setTitle("A message was deleted")
                        .setDescription(description)
                        .build();

                MessageCreateData mcd = new MessageCreateBuilder()
                        .setEmbeds(me)
                        .setComponents(createActionRowWithArchiveButton(msgId))
                        .build();

                session.getNSATX().sendMessage(mcd).queue();

                loggedMessage.setDeleted(); //Saved later
            }

            loggedMessage.getAttachmentList().stream()
                    .filter(la -> !la.isDeleted())
                    .forEach(la -> {
                        la.setDeleted(); //Saved later

                        String attUrl = attachmentUrl(la);
                        String description = String.format("""
                                        **%s:** %s
                                        **Member:** %s (%s)
                                                                            
                                        **File name:** %s
                                        **Content Type:** %s
                                        **NSA Link:** %s
                                        """,
                                channelTypeToString(event.getChannelType()),
                                event.getChannel().getName(),
                                memberMention,
                                userMention,
                                la.getName(),
                                la.getContentType(),
                                attUrl);

                        sendAttachmentDeletionLog(msgId, la, description);
                    });
            messageRepository.save(loggedMessage);
        }
    }

    private void sendAttachmentDeletionLog(long msgId, LoggedAttachment la, String embedDescription) {
        MessageEmbed me = new EmbedBuilder()
                .setColor(NSAUtil.NSA_DELETION_COLOR)
                .setTitle("An attachment was deleted")
                .setDescription(embedDescription)
                .build();

        MessageCreateData mcd = new MessageCreateBuilder()
                .setEmbeds(me)
                .setComponents(createActionRowWithArchiveButton(msgId))
                .build();

        MessageCreateAction embedMessageCreateAction = session.getNSATX().sendMessage(mcd);
        if (la.getContentType().contains("image") || la.getContentType().contains("video")) {
            MessageCreateAction mediaUrlMessageCreateAction = session.getNSATX().sendMessage(attachmentUrl(la));
            embedMessageCreateAction.and(mediaUrlMessageCreateAction).queue();
        } else {
            embedMessageCreateAction.queue();
        }
    }


    /**
     * We only want to log messages from the bound guild, and exclude the NSA channel to avoid echo
     *
     * @param event the event to perform the check on
     * @return true when it is a target channel for logging
     */
    private boolean inFocusChannel(@NonNull GenericMessageEvent event) {
        return event.isFromGuild()
                && event.getGuild().equals(session.getBoundGuild())
                && !event.getChannel().equals(session.getNSATX());
    }


    private String attachmentUrl(LoggedAttachment loggedAttachment) {
        return String.format("%s/attachments/%s/%s", baseUrl, loggedAttachment.getId(), loggedAttachment.getName());
    }

    private ActionRow createActionRowWithArchiveButton(Long messageId) {
        String url = String.format("%s/messages/%d", baseUrl, messageId);
        return ActionRow.of(Button.link(url, "Show History"));
    }
}
