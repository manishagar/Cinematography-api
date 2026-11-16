package com.cinematography.web.datasource;

import com.cinematography.core.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${cinematography.datasource.admin-path}")
@RequiredArgsConstructor
@Slf4j
public class DatabaseAdminController {

    private static final String ACTIVE_DATABASE = "activeDatabase";

    private final DynamicDatabaseAdminService databaseService;

    /**
     * Get current active DB
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, String>>> currentDatabase() {

        log.info("Fetching current active database");

        String activeDb = databaseService.currentDatabase();

        Map<String, String> data = Map.of(ACTIVE_DATABASE, activeDb);

        log.info("Current active database resolved: {}", activeDb);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Force DB switch using explicit DB name
     */
    @PostMapping("/switch/{dbName}")
    public ResponseEntity<ApiResponse<Map<String, String>>> switchDatabase(@PathVariable String dbName) {

        log.info("Requested database switch to '{}'", dbName);

        if (!StringUtils.hasText(dbName)) {
            log.warn("Database switch attempted with blank dbName");
            throw new IllegalArgumentException("Database name must not be blank");
        }

        String activeDb = databaseService.switchDatabase(dbName);

        Map<String, String> data = Map.of(ACTIVE_DATABASE, activeDb);

        log.info("Database switched successfully. Active database is now '{}'", activeDb);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(data));
    }

    /**
     * Refresh DB name internally and switch if changed
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshDatabase() {

        log.info("Refreshing database configuration");

        String activeDb = databaseService.refreshDatabase();

        Map<String, String> data = Map.of(ACTIVE_DATABASE, activeDb);

        log.info("Database refresh completed. Active database is '{}'", activeDb);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}