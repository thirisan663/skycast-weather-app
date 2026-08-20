package com.weatherapp.util;

import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lightweight anonymous-user identity via a long-lived cookie, so favorites
 * and search history persist across visits without requiring a login form.
 * Swap this out for real authentication (Spring Security) when needed -
 * every service method already takes a sessionToken String so the wiring
 * won't need to change, only how the token is derived.
 */
public final class SessionUtil {

    private static final String COOKIE_NAME = "weather_app_session";
    private static final int MAX_AGE_SECONDS = 60 * 60 * 24 * 365; // 1 year

    private SessionUtil() {
    }

    public static String resolveSessionToken(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String newToken = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(COOKIE_NAME, newToken);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return newToken;
    }
}
