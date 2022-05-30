package com.telluur.slapspring.model.ltg;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LTGGameRepository extends CrudRepository<LTGGame, Long> {
}