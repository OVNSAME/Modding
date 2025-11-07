package net.ovonsame.modding.enumeration.category;

import net.ovonsame.modding.interfaces.ICategory;

/**
 * Enumeration {@code ShaderCategory} is a subclass of {@code ICategory} and represents all {@code IntegrationType.SHADER} categories
 */
public enum ShaderCategory implements ICategory {
    VANILLA,
    MEDIUM,
    LOW,
    HIGH,
    SEMI_REALISTIC,
    FANTASY,
    REALISTIC,
    SHADOWS,
    PERFORMANCE,
    FEATURE
}
