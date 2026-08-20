package com.weatherapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-user display/unit preferences, e.g. Celsius vs Fahrenheit, theme, etc.
 */
@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 20, nullable = false)
    private String temperatureUnit = "CELSIUS"; // CELSIUS | FAHRENHEIT

    @Column(length = 20, nullable = false)
    private String windSpeedUnit = "KMH"; // KMH | MPH

    @Column(length = 20, nullable = false)
    private String theme = "AUTO"; // LIGHT | DARK | AUTO

    private Long defaultCityId;
}
