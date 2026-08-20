package com.weatherapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weatherapp.entity.SearchHistory;
import com.weatherapp.entity.User;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserOrderBySearchedAtDesc(User user);
    List<SearchHistory> findTop10ByUserOrderBySearchedAtDesc(User user);
    void deleteByUser(User user);
}
