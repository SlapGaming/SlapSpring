package com.telluur.slapspring.modules.nsa.model;

import org.springframework.data.repository.CrudRepository;

public interface LoggedMessageRepository extends CrudRepository<LoggedMessage, Long> {
}
