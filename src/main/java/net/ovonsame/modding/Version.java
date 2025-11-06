package net.ovonsame.modding;

import net.ovonsame.modding.interfaces.ILoader;

/**
 * Record class {@code Version} represents a Minecraft version with {@code ILoader} and version required
 * @see ILoader
 */
public record Version(ILoader loader, String version) {
    /**
     * @return String representation of the version like "loader-version"
     */
    @Override
    public String toString() {
        return loader.toString().toLowerCase() + "-" + version.toLowerCase();
    }

    /**
     * @return Whether the version is a snapshot
     */
    public boolean isSnapshot() {
        return version().chars().anyMatch(Character::isLetter);
    }
}
