package com.weatherapp.service;

import com.weatherapp.entity.User;

/** Provisions and resolves the lightweight session-based demo user. */
public interface UserService {
    User getOrCreateUser(String sessionToken);
}
