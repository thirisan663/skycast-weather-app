package com.weatherapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weatherapp.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);
    Optional<City> findByLatitudeAndLongitude(Double latitude, Double longitude);
}
