package net.ovonsame.modding;

import net.ovonsame.modding.enumeration.Platform;

/**
 * Record class with {@code Platform} and {@code String} id required in constructor. Designed to store general information about integration and further retrieval of complete information
 * @param platform Platform of the integration
 * @param identifier Identifier of the integration
 */
public record LazyIntegration(Platform platform, String identifier) {}
