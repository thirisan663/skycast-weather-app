package com.weatherapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A city a user has pinned for quick access on the Favorites page.
 */
@Entity
@Table(name = "favorite_cities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "city_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    /** Display order on the Favorites page (drag-to-reorder ready). */
    private Integer sortOrder = 0;
}
