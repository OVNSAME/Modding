package net.ovonsame.modding.interfaces;

import net.ovonsame.modding.Version;
import net.ovonsame.modding.enumiration.IntegrationType;
import net.ovonsame.modding.enumiration.Side;
import net.ovonsame.modding.enumiration.loader.*;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URL;
import java.util.*;

public interface IntegrationFile {
    /**
     * @return The changelog of the file
     */
    String getChangelog();

    /**
     * @return The name of the file
     */
    String getFileName();

    /**
     * @return The download {@code URL} of the file
     */
    URL getURL();

    /**
     * @return The size of the file
     */
    int getSize();

    /**
     * @return The publication date of the file
     * @see Date
     */
    Date getPublished();

    /**
     * @return The number of downloads of the file
     */
    int getFileDownloads();

    /**
     * @return Given loaders by the Platform
     * @see ILoader
     * @see ModLoader
     * @see PluginLoader
     * @see ShaderLoader
     */
    ILoader[] getLoaders();

    /**
     * @return Given versions by the Platform
     */
    String[] getVersions();

    /**
     * @return The type of the integration
     * @see IntegrationType
     */
    IntegrationType getType();

    /**
     * @return The environment side which this integration file is for
     * @see Side
     */
    Side getSide();

    /**
     * @return The integration this file is a part of
     * @see Integration
     */
    Integration getParentIntegration();

    /**
     * @return The unmodifiable collection of the dependencies this file requires
     */
    @Unmodifiable Collection<IntegrationFile> getDependencies();

    /**
     * @return The identifier of the integration
     */
    String getId();

    /**
     * @return The collection of the loaders available for this integration by the given publication date. Collection is unmodifiable
     */
    @Unmodifiable
    default Collection<ILoader> getPossibleLoaders() {
        final List<ILoader> available = new ArrayList<>();
        for (ILoader loader : getType().getLoaders()) {
            if (!loader.getCreated().after(getPublished())) {
                available.add(loader);
            }
        }

        return Collections.unmodifiableList(available);
    }

    /**
     * Combines all given versions and loaders from {@code getPossibleLoaders()} into one collection
     * @return Unmodifiable collection of the possible versions for this integration.
     */
    @Unmodifiable
    default Collection<Version> getPossibleVersions() {
        final var versions = getVersions();
        final List<ILoader> loaders = new ArrayList<>(Arrays.stream(getLoaders()).toList());

        if(loaders.contains(ILoader.ANY) && loaders.size() <= 1) loaders.addAll(getPossibleLoaders());

        final Version[] v = new Version[loaders.size() * versions.length];
        for(int i = 0; i < loaders.size(); i++) {
            for(int j = 0; j < versions.length; j++) {
                v[i * versions.length + j] = new Version(loaders.get(i), versions[j]);
            }
        }
        return Collections.unmodifiableCollection(Arrays.asList(v));
    }
}
