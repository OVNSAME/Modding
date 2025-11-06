package net.ovonsame.modding.enumiration;

import net.ovonsame.modding.enumiration.category.*;
import net.ovonsame.modding.enumiration.loader.*;
import net.ovonsame.modding.interfaces.ICategory;
import net.ovonsame.modding.interfaces.ILoader;

/**
 * Enumeration {@code IntegrationType} represents all possible types of Minecraft integrations
 */
public enum IntegrationType {
    MOD(ModLoader.values(), ModCategory.values()),
    PLUGIN(PluginLoader.values(), PluginCategory.values()),
    DATAPACK(DatapackCategory.values()),
    RESOURCEPACK(ResourcepackCategory.values()),
    SHADER(ShaderLoader.values(), ShaderCategory.values()),
    MODPACK(ModLoader.values(), ModpackCategory.values()),
    ADDON(AddonCategory.values()),
    CUSTOMIZATION(CustomizationCategory.values()),
    WORLD(WorldCategory.values());

    private final ILoader[] loaders;
    private final ICategory[] categories;

    IntegrationType(ILoader[] loaders, ICategory[] categories) {
        this.loaders = loaders;
        this.categories = categories;
    }

    IntegrationType(ICategory[] categories) {
        this.loaders = new ILoader[]{ILoader.ANY};
        this.categories = categories;
    }

    public final ILoader[] getLoaders() {
        return loaders;
    }

    public final ICategory[] getCategories() {
        return categories;
    }
}
