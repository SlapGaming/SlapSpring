package com.telluur.slapspring.model.ltg;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LTGGameRepository extends PagingAndSortingRepository<LTGGame, Long> {
}