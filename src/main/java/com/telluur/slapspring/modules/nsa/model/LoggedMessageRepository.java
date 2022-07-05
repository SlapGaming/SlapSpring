package com.telluur.slapspring.modules.nsa.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggedMessageRepository extends CrudRepository<LoggedMessage, Long> {
}
