package com.weatherapp.exception;

/** Thrown when a searched city/country cannot be geocoded to coordinates. */
public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String query) {
        super("Could not find a location matching \"" + query + "\". Try a different spelling or add a country code.");
    }
}
