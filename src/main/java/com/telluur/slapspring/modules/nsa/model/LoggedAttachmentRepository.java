package com.telluur.slapspring.modules.nsa.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggedAttachmentRepository extends CrudRepository<LoggedAttachment, Long> {
}
