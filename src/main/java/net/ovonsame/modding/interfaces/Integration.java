package net.ovonsame.modding.interfaces;

import net.ovonsame.modding.Version;
import net.ovonsame.modding.enumeration.*;
import net.ovonsame.modding.enumeration.category.*;
import net.ovonsame.modding.enumeration.loader.ModLoader;
import net.ovonsame.modding.interfaces.authority.*;
import org.jetbrains.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public interface Integration extends Iterable<IntegrationFile> {
    default @NotNull Iterator<IntegrationFile> iterator() {
        return getFiles().iterator();
    }

    /**
     * @return A title of the integration
     */
    String getTitle();

    /**
     * @return A slug of the integration.
     */
    String getSlug();

    /**
     * @return The full description of the integration
     */
    String getFullDescription();

    /**
     * @return An identifier of the integration
     */
    String getId();

    /**
     * @return A license of the integration
     */
    String getLicense();

    /**
     * @return An edition of the integration
     */
    Edition getEdition();

    /**
     * @return Organisation which owns the project. Can be null because projects can be created by individuals
     * @see Organisation
     */
    @NotNull
    default Organisation getOrganisation() {
        var a = getAuthors();
        return new Organisation() {
            @Override
            public Author getOwner() {
                return a.iterator().next();
            }

            @Override
            public String getName() {
                return getCleanTitle() + " Organisation";
            }

            @Override
            public @NotNull Iterator<Author> iterator() {
                return a.iterator();
            }
        };
    }

    /**
     * @return The team name which has created the project. Can be null as projects can be created by individuals
     * @see Team
     */
    @NotNull
    default Team getTeam() {
        return new Team() {
            @Override
            public String getName() {
                return getCleanTitle() + " Team";
            }

            @Override
            public @NotNull Iterator<Author> iterator() {
                return getAuthors().iterator();
            }
        };
    }

    /**
     * @return Set of all authors
     * @see Author
     */
    Set<Author> getAuthors();

    /**
     * @return A date when the integration was published
     * @see Date
     */
    Date getPublished();

    /**
     * @return A date when the integration was last updated
     * @see Date
     */
    Date getUpdated();

    /**
     * @return A date of approval of the integration. Can be null as it is not always given
     * @see Date
     */
    @Nullable Date getApproved();

    /**
     * @return A type of the integration
     * @see IntegrationType
     */
    IntegrationType getType();

    /**
     * @return All categories of the integration
     * @see ICategory
     * @see AddonCategory
     * @see ModCategory
     * @see PluginCategory
     * @see ShaderCategory
     * @see WorldCategory
     * @see DatapackCategory
     * @see ModpackCategory
     * @see ResourcepackCategory
     * @see CustomizationCategory
     */
    Set<ICategory> getCategories();

    /**
     * @return The only status of the integration
     * @see Status
     */
    Status getStatus();

    /**
     * @return Total downloads of the integration
     */
    int getDownloads();

    /**
     * @return Total likes of the integration
     */
    int getLikes();

    /**
     * @return Whether the integration is premium. False by default
     */
    default boolean isPremium() {
        return false;
    }

    /**
     * @return The {@code URL} of the integration icon. Nullable because not all integrations have an icon
     */
    @Nullable URL getIcon();

    /**
     * @return The {@code URL} of the integration issues. Nullable because not all integrations have an open issue tracker
     */
    @Nullable URL getIssues();

    /**
     * @return The {@code URL} of the integration wiki page. Nullable as not all integrations have a wiki
     */
    @Nullable URL getWiki();

    /**
     * @return The {@code URL} of the integration source code. Nullable as not all integrations have an open source code
     */
    @Nullable URL getSource();

    /**
     * @return The {@code URL} of the integration donation page. Nullable as not all integrations have a donation page
     */
    @Nullable URL getDonation();

    /**
     * @return The {@code URL} array of the integration screenshots. Can be empty because not all integrations have screenshots
     */
    URL[] getScreenshots();

    /**
     * @return All files of the integration with maximum size of 10000 files. Unmodifiable by default
     * @see IntegrationFile
     */
    @Unmodifiable Collection<IntegrationFile> getFiles();

    /**
     * @return The Platform where the integration was taken
     * @see Platform
     */
    Platform getPlatform();

    /**
     * @return A clear name without special characters and brackets
     */
    default String getCleanTitle() {
        return getTitle()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\[.*?]", "")
                .replaceAll("\\{.*?}", "")
                .replaceAll("<.*?>", "")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "")
                .trim();
    }

    /**
     * @return The implementation type of the integration
     * @see ImplementationType
     */
    default ImplementationType getImplementationType() {
        return switch (getType()) {
            case MOD -> ImplementationType.MAVEN;
            case PLUGIN, ADDON -> ImplementationType.DOWNLOAD;
            default -> ImplementationType.NONE;
        };
    }

    /**
     * Generates a definition file for the integration with the name as the slug and the extension as {@code .yaml}.
     * File contains main section as the version name and "gradle" section in every version.
     * Gradle sections shows how to add the integration with dependencies to your build.gradle file for specific version.
     * If the integration is not {@code IntegrationType.MOD}, {@code IntegrationType.PLUGIN} or {@code IntegrationType.ADDON}, file won't be generated.
     * @param directory The directory to generate the definition file in
     */
    default void generate(final File directory) {
        final Set<Version> processed = new HashSet<>();
        final IntegrationType type = getType();
        final ImplementationType impl = getImplementationType();
        final boolean curseforge = getPlatform() == Platform.CURSEFORGE;

        if(!directory.isDirectory() || impl == ImplementationType.NONE) return;

        final String slug = getSlug().replace("-", "_");
        final String name = getCleanTitle();

        final File file = new File(directory.toString(), slug + ".yaml");

        try (final FileWriter fw = new FileWriter(file)) {
            fw.write("---\n");
            for(IntegrationFile integ : getFiles()) {
                if(integ.getURL() == null) continue;

                for(Version version : integ.getPossibleVersions()) {
                    if(version.isSnapshot() || processed.contains(version)) continue;
                    processed.add(version);

                    fw.write(version + ":\n");
                    final boolean fabric = version.loader() == ModLoader.FABRIC || version.loader() == ModLoader.QUILT;

                    if(impl == ImplementationType.MAVEN) {
                        if(curseforge) {
                            fw.write(
                                    "  gradle: |\n" +
                                        "    var final gradlev = gradle.gradleVersion.replaceAll(/[^\\d.]/, '').toBigDecimal()\n" +
                                        "    if (gradlev >= 6.2) {\n" +
                                        "        repositories {\n" +
                                        "            exclusiveContent {\n" +
                                        "                forRepository {\n" +
                                        "                    maven {\n" +
                                        "                        url \"https://cursemaven.com\"\n" +
                                        "                    }\n" +
                                        "                }\n\n" +
                                        "                filter {\n" +
                                        "                    includeGroup \"curse.maven\"\n" +
                                        "                }\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    } else if (gradlev >= 5) {\n" +
                                        "        repositories {\n" +
                                        "            maven {\n" +
                                        "                url \"https://cursemaven.com\"\n" +
                                        "                content {\n" +
                                        "                    includeGroup \"curse.maven\"\n" +
                                        "                }\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    } else {\n" +
                                        "        repositories {\n" +
                                        "            maven {\n" +
                                        "                url \"https://cursemaven.com\"\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    }\n\n"
                            );
                        } else {
                            fw.write(
                                    "  gradle: |\n" +
                                        "    var final gradlev = gradle.gradleVersion.replaceAll(/[^\\d.]/, '').toBigDecimal()\n" +
                                        "    if (gradlev >= 6.2) {\n" +
                                        "        repositories {\n" +
                                        "            exclusiveContent {\n" +
                                        "                forRepository {\n" +
                                        "                    maven {\n" +
                                        "                        name = \"Modrinth\"\n" +
                                        "                        url = \"https://api.modrinth.com/maven\"\n" +
                                        "                    }\n" +
                                        "                }\n\n" +
                                        (!fabric ? "                forRepositories(fg.repository)\n\n" : "") +
                                        "                filter {\n" +
                                        "                    includeGroup \"maven.modrinth\"\n" +
                                        "                }\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    } else {\n" +
                                        "        repositories {\n" +
                                        "            maven {\n" +
                                        "                url = \"https://api.modrinth.com/maven\"\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    }\n\n"
                            );
                        }

                        final Collection<IntegrationFile> dependencies = new ArrayList<>(integ.getDependencies());
                        dependencies.add(integ);

                        fw.write("    dependencies {\n");

                        for (IntegrationFile dependency : dependencies) {
                            Integration integration = dependency.getParentIntegration();
                            fw.write(
                                    "        " + (fabric ? "modImplementation" : "implementation fg.deobf") +
                                    "('" + (curseforge ? "curse.maven:" + integration.getSlug() + "-" + integration.getId() + ":" + dependency.getId()
                                    : "maven.modrinth:" + integration.getSlug() + ":" + dependency.getId()) + "')\n"
                            );
                        }

                        fw.write("    }\n\n");
                    } else {
                        fw.write(
                            "  gradle: |\n" +
                                "    tasks.register('" + slug + "') {\n" +
                                "      ant.mkdir(dir: 'lib/');\n" +
                                "      ant.get(src: '" + integ.getURL() + "', dest: 'lib/" + slug + ".jar', skipexisting: 'true');\n" +
                                "    }\n\n"
                        );

                        fw.write(
                            "    compileJava.dependsOn " + slug + "\n\n" +
                                "    dependencies { implementation files(\"lib/" + slug + ".jar\") }\n\n"
                        );
                    }

                    fw.write(
                        "  update_files:\n" +
                            "    - ~\n\n"
                    );
                }
            }

            fw.write("name: \"" + name + "\"\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
