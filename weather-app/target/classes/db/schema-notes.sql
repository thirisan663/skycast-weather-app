-- ============================================================
-- SCHEMA NOTES
-- Tables are created/updated automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) from the entities in
-- com.weatherapp.entity. This file documents the resulting shape
-- for reference and is what ddl-auto=validate would expect in
-- a stricter production setup. Run manually only if you set
-- spring.jpa.hibernate.ddl-auto=none and want to manage schema
-- migrations yourself (e.g. via Flyway/Liquibase).
-- ============================================================

CREATE DATABASE IF NOT EXISTS weather_app_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE weather_app_db;

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_token   VARCHAR(64) NOT NULL UNIQUE,
    display_name    VARCHAR(100),
    created_at      DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS cities (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    state       VARCHAR(100),
    country     VARCHAR(100),
    latitude    DOUBLE NOT NULL,
    longitude   DOUBLE NOT NULL,
    UNIQUE KEY uq_city (name, country, latitude, longitude)
);

CREATE TABLE IF NOT EXISTS search_history (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    city_id                 BIGINT NOT NULL,
    searched_at             DATETIME NOT NULL,
    temperature_at_search   DOUBLE,
    condition_at_search     VARCHAR(100),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

CREATE TABLE IF NOT EXISTS favorite_cities (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    city_id     BIGINT NOT NULL,
    added_at    DATETIME NOT NULL,
    sort_order  INT DEFAULT 0,
    UNIQUE KEY uq_user_city (user_id, city_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,
    temperature_unit    VARCHAR(20) NOT NULL DEFAULT 'CELSIUS',
    wind_speed_unit     VARCHAR(20) NOT NULL DEFAULT 'KMH',
    theme               VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    default_city_id     BIGINT,
    CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
