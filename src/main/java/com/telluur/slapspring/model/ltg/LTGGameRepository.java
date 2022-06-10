package com.telluur.slapspring.model.ltg;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LTGGameRepository extends PagingAndSortingRepository<LTGGame, Long> {
}