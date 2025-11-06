package net.ovonsame.modding.interfaces;

/**
 * Interface {@code ICategory} is a representation of a Minecraft category and is used to group {@code Integration}s.
 */
public interface ICategory {
    /**
     * {@code ICategory.CURSED} used to lessen the amount of values in enums because {@code Platform.MODRINTH} platform has "CURSED" in at least every category
     */
    ICategory CURSED = new ICategory() {};
}
