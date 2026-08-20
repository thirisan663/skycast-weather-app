package com.weatherapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weatherapp.entity.City;
import com.weatherapp.entity.FavoriteCity;
import com.weatherapp.entity.User;

public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    List<FavoriteCity> findByUserOrderBySortOrderAsc(User user);
    Optional<FavoriteCity> findByUserAndCity(User user, City city);
    boolean existsByUserAndCity(User user, City city);
}
