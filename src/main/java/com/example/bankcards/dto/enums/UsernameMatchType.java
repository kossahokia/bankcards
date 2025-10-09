package com.example.bankcards.dto.enums;

/**
 * Enum representing the type of username matching used in user search filters.
 * <p>
 * This enumeration defines how username-based queries are compared when
 * searching for users in administrative endpoints (e.g. {@code /api/admin/users?username=...}).
 * </p>
 *
 * <ul>
 *   <li>{@link #EQUALS} – exact match (username must fully match the query)</li>
 *   <li>{@link #STARTS} – username must start with the query string</li>
 *   <li>{@link #CONTAINS} – username must contain the query string anywhere</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public enum UsernameMatchType {

    /**
     * Exact match — username must be identical to the query.
     */
    EQUALS,

    /**
     * Match usernames that start with the query string.
     */
    STARTS,

    /**
     * Match usernames that contain the query string anywhere.
     */
    CONTAINS
}
