package com.telluur.slapspring.modules.nsa.model;

import lombok.*;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Date;
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

    private long referencedMessageId;

    private String jumpUrl;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date(System.currentTimeMillis());

    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "nsa_logged_messagecontent")
    @OrderColumn
    @Builder.Default
    private List<LoggedMessageContent> contentHistory = new LinkedList<>();


    @OneToMany(mappedBy = "loggedMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoggedAttachment> attachmentList = new LinkedList<>();

    @Transactional
    public void appendContentHistory(String contentRaw) {
        LoggedMessageContent lmc = LoggedMessageContent.builder().contentRaw(contentRaw).build();
        contentHistory.add(lmc);
    }

    public boolean isDeleted() {
        return deletedDate != null;
    }

    public void setDeleted() {
        deletedDate = new Date(System.currentTimeMillis());
    }

}
