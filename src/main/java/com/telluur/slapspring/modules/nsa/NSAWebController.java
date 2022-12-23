package com.telluur.slapspring.modules.nsa;

import com.telluur.slapspring.core.discord.BotSession;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachment;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachmentRepository;
import com.telluur.slapspring.modules.nsa.model.LoggedMessage;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageRepository;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;

import static com.telluur.slapspring.util.discord.DiscordUtil.channelTypeToString;


@Controller
public class NSAWebController {

    @Autowired
    LoggedMessageRepository messageRepository;

    @Autowired
    LoggedAttachmentRepository attachmentRepository;

    @Autowired
    BotSession botSession;


    @Transactional
    @GetMapping(value = "/messages/{messageId}")
    String messageHistoryThymeleaf(@PathVariable Long messageId, Model model) {
        LoggedMessage loggedMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("loggedMessage", loggedMessage);

        String userName;
        long userId = loggedMessage.getUserId();
        User user = botSession.getJda().getUserById(userId);
        if (user != null) {
            Member member = botSession.getBoundGuild().getMemberById(userId);
            if (member != null) {
                userName = String.format("%s (%s)", member.getEffectiveName(), user.getAsTag());
            } else {
                userName = user.getAsTag();
            }
        } else {
            userName = String.valueOf(userId);
        }
        model.addAttribute("userName", userName);

        long channelId = loggedMessage.getChannelId();
        TextChannel tx = botSession.getJda().getTextChannelById(channelId);
        String channelName;
        if (tx != null) {
            channelName = String.format("%s (%s)", tx.getName(), channelTypeToString(tx.getType()));
        } else {
            channelName = String.valueOf(channelId);
        }
        model.addAttribute("channelName", channelName);

        return "message-history";
    }


    @GetMapping(value = "/attachments/{attachmentId}")
    ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        LoggedAttachment la = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ByteArrayResource bar = new ByteArrayResource(la.getContent());


        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType mediaType = MediaType.parseMediaType(la.getContentType());
            responseHeaders.setContentType(mediaType);
            return new ResponseEntity<Resource>(bar, responseHeaders, HttpStatus.OK);
        } catch (InvalidMediaTypeException | InvalidMimeTypeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not determine the MimeType of the requested resource.");
        }
    }

    @GetMapping(value = "/attachments/{attachmentId}/{ignoredFileName}")
    ResponseEntity<Resource> downloadAttachmentWithFilePlaceholder(@PathVariable Long attachmentId) {
        LoggedAttachment la = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ByteArrayResource bar = new ByteArrayResource(la.getContent());


        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType mediaType = MediaType.parseMediaType(la.getContentType());
            responseHeaders.setContentType(mediaType);
            return new ResponseEntity<Resource>(bar, responseHeaders, HttpStatus.OK);
        } catch (InvalidMediaTypeException | InvalidMimeTypeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not determine the MimeType of the requested resource.");
        }
    }

}
