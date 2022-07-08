package com.telluur.slapspring.modules.nsa;

import com.telluur.slapspring.modules.nsa.model.LoggedAttachment;
import com.telluur.slapspring.modules.nsa.model.LoggedAttachmentRepository;
import com.telluur.slapspring.modules.nsa.model.LoggedMessage;
import com.telluur.slapspring.modules.nsa.model.LoggedMessageRepository;
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


@Controller
public class NSAWebController {

    @Autowired
    LoggedMessageRepository messageRepository;

    @Autowired
    LoggedAttachmentRepository attachmentRepository;



    @Transactional
    @GetMapping(value = "/messages/{messageId}")
    String messageHistoryThymeleaf(@PathVariable Long messageId, Model model){
        LoggedMessage loggedMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        //noinspection ResultOfMethodCallIgnored: Force fetch of lazy content
        //loggedMessage.getContentHistory().size();
        //noinspection ResultOfMethodCallIgnored: Force fetch of lazy content
        //loggedMessage.getAttachmentList().size();




        model.addAttribute(loggedMessage);

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
