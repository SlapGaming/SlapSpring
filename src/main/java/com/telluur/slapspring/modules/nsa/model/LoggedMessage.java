package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

//JPA
@Entity
@Table(name = "nsa_logged_message")

//Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoggedMessage {
    @Id
    private long id;

    @Column(nullable = false)
    private long channelId, userId;

    @Builder.Default
    private boolean deleted = false;

    private String jumpUrl;
    //@ElementCollection(fetch = FetchType.EAGER)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "nsa_logged_messagecontent")
    @OrderColumn
    @Builder.Default
    private List<LoggedMessageContent> contentHistory = new LinkedList<>();


    @OneToMany(mappedBy="loggedMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoggedAttachment> attachmentList = new LinkedList<>();

    @Transactional
    public void appendContentHistory(String contentRaw) {
        LoggedMessageContent lmc = new LoggedMessageContent();
        lmc.setContentRaw(contentRaw);
        contentHistory.add(lmc);
    }

}
