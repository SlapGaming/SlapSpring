package com.telluur.slapspring.modules.joinnotifier.model;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinMessageRepository extends CrudRepository<JoinMessage, Long> {

}
