package com.telluur.slapspring.services.discord.impl.ltg.model;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LTGGameRepository extends PagingAndSortingRepository<LTGGame, Long> {
}