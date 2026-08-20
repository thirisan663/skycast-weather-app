package com.weatherapp.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weatherapp.entity.User;
import com.weatherapp.entity.UserPreference;
import com.weatherapp.repository.UserRepository;
import com.weatherapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(String sessionToken) {
        return userRepository.findBySessionToken(sessionToken)
                .orElseGet(() -> {
                    User user = new User();
                    user.setSessionToken(sessionToken);
                    user.setDisplayName("Guest");

                    UserPreference preference = new UserPreference();
                    preference.setUser(user);
                    user.setPreference(preference);

                    return userRepository.save(user);
                });
    }
}
