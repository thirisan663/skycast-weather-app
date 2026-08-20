# SkyCast — AI-Powered Weather Dashboard

A production-styled weather web application built with **Spring Boot, MySQL, Thymeleaf, Bootstrap 5, and vanilla JavaScript**, featuring an AI weather assistant grounded in live conditions.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.3 (Web, Data JPA, Validation, Cache) |
| Database | MySQL 8 |
| Server-side rendering | Thymeleaf |
| Frontend | HTML5, CSS3, Bootstrap 5, Bootstrap Icons |
| JavaScript | Vanilla JS (no framework, no build step) |
| External data | Pluggable weather provider (OpenWeatherMap-shaped REST API) |
| AI Assistant | Pluggable AI provider (Anthropic Messages API shape) |

## Project Structure

```
weather-app/
├── pom.xml
└── src/main/
    ├── java/com/weatherapp/
    │   ├── WeatherAppApplication.java
    │   ├── controller/     REST + page controllers
    │   ├── service/        interfaces + impl/ (business logic, external API clients)
    │   ├── repository/     Spring Data JPA repositories
    │   ├── entity/         JPA entities (User, City, SearchHistory, FavoriteCity, UserPreference)
    │   ├── dto/             request/response DTOs (controllers never expose entities)
    │   ├── config/          RestTemplate beans, typed @ConfigurationProperties, CORS
    │   ├── exception/       custom exceptions + @RestControllerAdvice
    │   └── util/            SessionUtil (anonymous session cookie)
    └── resources/
        ├── application.properties
        ├── db/schema-notes.sql       (reference schema; Hibernate auto-creates it)
        ├── templates/
        │   ├── fragments/            navbar, footer, head, scripts, ai-chat, empty-state
        │   └── pages/                index, forecast, favorites, history, assistant
        └── static/
            ├── css/   variables.css, base.css, layout.css, components.css, animations.css, responsive.css
            └── js/    utils.js, theme.js, weather.js, ai-assistant.js, favorites.js, history.js
```

## Getting Started

### 1. Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8 running locally (or update the connection URL for a remote instance)

### 2. Database
Nothing to run manually — with `spring.jpa.hibernate.ddl-auto=update`, Hibernate creates/updates
the schema automatically from the JPA entities on startup. `db/schema-notes.sql` documents the
resulting tables for reference, or for use with a real migration tool (Flyway/Liquibase) in production.

Create the database user/password referenced in `application.properties`, or override via environment
variables / a `application-local.properties` profile.

### 3. API Keys
Set these environment variables before running (never commit real keys):

```bash
export WEATHER_API_KEY=your_openweathermap_key
export AI_API_KEY=your_ai_provider_key
```

The app is written against an OpenWeatherMap-shaped API (`/weather`, `/forecast`, `/onecall`,
`/geo/1.0/direct`) and an Anthropic-shaped Messages API. Swap `WeatherProviderClient` or
`AiAssistantServiceImpl` if you use a different provider — every other class only depends on
this app's own DTOs, so the blast radius of a provider swap is contained to those two files.

### 4. Run

```bash
mvn spring-boot:run
```

Visit `http://localhost:8080`.

## Key Design Decisions

- **DTO boundary**: controllers and Thymeleaf/JS never see JPA entities directly — `WeatherService`
  and friends map external API responses and entities into dedicated DTOs.
- **Anonymous sessions**: a long-lived `weather_app_session` cookie identifies a visitor without
  requiring login, so favorites/history work out of the box. `SessionUtil` is the only place this
  lives — swapping in Spring Security later means updating one utility, not every controller.
- **Caching**: `@Cacheable` on `WeatherService.getByCoordinates` avoids redundant calls to the
  weather provider for repeat lookups of the same coordinates within the cache TTL.
- **AI grounding**: `AiAssistantServiceImpl` builds a system prompt from the *live* weather data for
  whatever city is on screen (temperature, condition, wind, UV, sunrise/sunset, rain chance) before
  calling the AI provider, so answers about umbrellas/clothing/outdoor plans are based on real numbers.
- **Responsive layout**: `.dashboard-grid` is a flex column that becomes a two-column row at the
  `lg` breakpoint (current weather + forecast on the left, AI assistant + insights on the right, sticky).
  Below `lg` everything stacks single-column: search → current weather → hourly (horizontally
  scrollable) → 7-day → AI assistant, exactly per the mobile spec.
- **No horizontal scroll**: `overflow-x: hidden` on `body`, `min-width: 0` on all flex children, and
  the hourly forecast strip is the *only* intentionally horizontally-scrollable region (`.scroll-strip`).

## Notes on Going to Production

- Replace the demo API keys and MySQL credentials with real secrets management (not properties files).
- Add Spring Security if you need real authenticated accounts instead of the anonymous-cookie model.
- Add Flyway/Liquibase migrations instead of `ddl-auto=update` once the schema stabilizes.
- The UV index field in `CurrentWeatherDto` is left null-safe because it isn't part of every
  weather provider's free tier — wire it up via a One Call 3.0 (or equivalent) endpoint if available.
