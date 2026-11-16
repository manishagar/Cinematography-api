package com.cinematography.web.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Country code → country name lookup, loaded once at startup.
 * <p>
 * Registered when {@code cinematography.cache.country-query} is set. The query must return the country code in the
 * first column and the country name in the second, e.g. {@code SELECT ISO2, Name FROM Country}.
 */
@Slf4j
public class CountryCodeCache {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String countryQuery;

    protected static final Map<String, String> loadCountryCodeName = new HashMap<>();

    public CountryCodeCache(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String countryQuery) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.countryQuery = countryQuery;
    }

    @PostConstruct
    public void init() {
        loadCountryCodes();
    }

    private void loadCountryCodes() {
        if (loadCountryCodeName.isEmpty()) {
            log.info("CountryCodeCache loadCountryCodes from the DB");
            try {
                namedParameterJdbcTemplate.query(countryQuery, new MapSqlParameterSource(), (rs, rowNum) -> {
                    loadCountryCodeName.put(rs.getString(1).toUpperCase().trim(), rs.getString(2).trim());
                    return null;
                });
            } catch (Exception se) {
                log.error("Error occurred in CountryCodeCache loadCountryCodes: {}", se.getMessage());
            }
        } else {
            log.info("CountryCodeCache loadCountryCodes is already loaded. Size: {}", getLoadCountryCodeName().size());
        }
    }

    public static Map<String, String> getLoadCountryCodeName() {
        return Collections.unmodifiableMap(loadCountryCodeName);
    }
}
