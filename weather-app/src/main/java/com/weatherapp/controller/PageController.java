package com.weatherapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the Thymeleaf pages. All actual data (current weather, forecast,
 * favorites, history) is loaded client-side via the REST API in weather.js /
 * favorites.js / history.js - these routes just render the page shell and
 * pass along which nav item should be marked active.
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        model.addAttribute("pageTitle", "Dashboard");
        return "pages/index";
    }

    @GetMapping("/forecast")
    public String forecast(Model model) {
        model.addAttribute("activePage", "forecast");
        model.addAttribute("pageTitle", "Forecast");
        return "pages/forecast";
    }

    @GetMapping("/favorites")
    public String favorites(Model model) {
        model.addAttribute("activePage", "favorites");
        model.addAttribute("pageTitle", "Favorites");
        return "pages/favorites";
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("activePage", "history");
        model.addAttribute("pageTitle", "Search History");
        return "pages/history";
    }

    @GetMapping("/assistant")
    public String assistant(Model model) {
        model.addAttribute("activePage", "assistant");
        model.addAttribute("pageTitle", "AI Assistant");
        return "pages/assistant";
    }
}
