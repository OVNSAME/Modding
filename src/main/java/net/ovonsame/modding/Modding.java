package net.ovonsame.modding;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPInputStream;

import net.ovonsame.modding.enumiration.*;
import net.ovonsame.modding.enumiration.category.*;
import net.ovonsame.modding.enumiration.loader.*;
import net.ovonsame.modding.interfaces.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import static net.ovonsame.modding.enumiration.Status.*;
import static net.ovonsame.modding.enumiration.Platform.*;
import static net.ovonsame.modding.enumiration.IntegrationType.*;
import static net.ovonsame.modding.enumiration.Side.*;

/**
 * Class {@code Modding} contains private wrapper classes for each supported integration platform which implements specific interfaces
 * @see IntegrationWrapper
 * @see Integration
 */
public final class Modding {
    private static final Gson GSON = new Gson();

    /**
     * Method ables you to get any integration from any supported platform and work with it
     * @param platform Platform to get the integration from
     * @param identifier Identifier of the integration
     * @param key Optional key which is required for some platforms
     * @return Integration from the specified platform with the specified identifier
     * @throws IOException If the integration is not found, key is not provided or is invalid and the platform requires it, if some errors occurs while connecting with the platform
     * @see Integration
     * @see IntegrationWrapper
     * @see Platform
     */
    public static Integration getIntegration(final Platform platform, final String identifier, final @Nullable String key) throws IOException {
        return switch (platform) {
            case CURSEFORGE -> new CurseforgeWrapper(identifier, key);
            case MODRINTH -> new ModrinthWrapper(identifier, key);
            case SPIGET -> new SpigetWrapper(identifier, key);
        };
    }

    /**
     * The main abstract wrapper class for all platforms wrappers. It implements the {@link Integration} interface
     */
    private static abstract class IntegrationWrapper implements Integration {
        protected final String modid;
        protected final @Nullable String key;

        public IntegrationWrapper(final String modid, final @Nullable String key) {
            this.modid = modid;
            this.key = key;
        }

        protected abstract String connect(final String endpoint, final @Nullable String key) throws IOException;

        protected JsonObject getResponse(final String endpoint, final @Nullable String key) throws IOException {
            return GSON.fromJson(connect(endpoint, key), JsonObject.class);
        }

        protected JsonArray getResponseArray(final String endpoint, final @Nullable String key) throws IOException {
            return GSON.fromJson(connect(endpoint, key), JsonArray.class);
        }
    }

    /**
     * {@code ModrinthWrapper} is a wrapper class for Modrinth platform
     */
    private static final class ModrinthWrapper extends IntegrationWrapper {
        private final JsonObject data;
        private final JsonArray versions;

        public ModrinthWrapper(final String modid, final @Nullable String key) throws IOException {
            super(modid, key);
            data = getResponse("/project/" + modid, key);
            versions = getResponseArray("/project/" + modid + "/version", key);
        }

        @Override
        protected String connect(String endpoint, @Nullable String key) throws IOException {
            final URL url = new URL(MODRINTH.getUrl() + endpoint);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "ModdingLibrary/1.0");
            if(MODRINTH.isKeyRequired()) {
                if(key != null) {
                    con.setRequestProperty("x-api-key", key);
                } else {
                    throw new IOException("API key is required");
                }
            }
            final int code = con.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                throw new RuntimeException("Failed to fetch data: HTTP error code " + code);
            }
        }

        @Override
        public String getTitle() {
            return data.get("title").getAsString();
        }

        @Override
        public String getSlug() {
            return data.get("slug").getAsString();
        }

        @Override
        public String getFullDescription() {
            return data.get("body").getAsString();
        }

        @Override @Nullable
        public String getTeam() {
            return data.has("team") ? data.get("team").getAsString() : null;
        }

        @Override
        public IntegrationType getType() {
            return IntegrationType.valueOf(data.get("project_type").getAsString().toUpperCase());
        }

        @Override
        public String getId() {
            return data.get("id").getAsString();
        }

        @Override
        public String getLicense() {
            return data.get("license").getAsJsonObject().get("id").getAsString();
        }

        @Override @Nullable
        public String getOrganisation() {
            return data.has("organization") ? data.get("organization").getAsString() : null;
        }

        @Override
        public Date getPublished() {
            return Date.from(Instant.parse(data.get("published").getAsString()));
        }

        @Override
        public Date getUpdated() {
            return Date.from(Instant.parse(data.get("updated").getAsString()));
        }

        @Override @Nullable
        public Date getApproved() {
            return data.has("approved") ? Date.from(Instant.parse(data.get("approved").getAsString())) : null;
        }

        @Override
        public Status getStatus() {
            final String s = data.get("status").getAsString();
            try {
                return Status.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Status.UNKNOWN;
            }
        }

        @Override
        public int getDownloads() {
            return data.get("downloads").getAsInt();
        }

        @Override
        public int getLikes() {
            return data.get("followers").getAsInt();
        }

        @Override @Nullable
        public URL getIcon() {
            try {
                final String i = data.get("icon_url").getAsString();
                if (i == null || i.isEmpty()) return null;
                return new URL(i);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getDonation() {
            try {
                final String d = data.get("donation_urls").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                if (d == null || d.isEmpty()) return null;
                return new URL(d);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException | IndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getIssues() {
            try {
                final String i = data.get("issues_url").getAsString();
                if (i == null || i.isEmpty()) return null;
                return new URL(i);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getWiki() {
            try {
                final String w = data.get("wiki_url").getAsString();
                if (w == null || w.isEmpty()) return null;
                return new URL(w);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getSource() {
            try {
                final String s = data.get("source_url").getAsString();
                if (s == null || s.isEmpty()) return null;
                return new URL(s);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override
        public Set<String> getAuthors() {
            try {
                final String team = data.get("team").getAsString();
                final JsonArray members = getResponseArray("/team/" + team + "/members", key);
                final Set<String> authors = new HashSet<>(members.size());
                for (JsonElement m : members) {
                    final JsonObject member = m.getAsJsonObject();
                    final String name = member.get("user").getAsJsonObject().get("username").getAsString();
                    authors.add(name);
                }
                return authors;
            } catch (Exception e) {
                return Collections.emptySet();
            }
        }

        @Override
        public Set<ICategory> getCategories() {
            final JsonArray categories = data.get("categories").getAsJsonArray();
            final Set<ICategory> set = new HashSet<>();
            for (JsonElement c : categories) {
                String s = c.getAsString();
                ICategory t = switch (getType()) {
                    case MOD -> switch (s) {
                        case "adventure" -> ModCategory.RPG;
                        case "game-mechanics", "minigame" -> ModCategory.MISCELLANEOUS;
                        case "decoration" -> ModCategory.COSMETIC;
                        case "economy" -> ModCategory.EDUCATION;
                        case "equipment" -> ModCategory.EQUIPMENT;
                        case "food" -> ModCategory.FOOD;
                        case "library" -> ModCategory.LIBRARY;
                        case "magic" -> ModCategory.MAGIC;
                        case "management", "utility" -> ModCategory.UTILITIES;
                        case "mobs" -> ModCategory.MOBS;
                        case "optimization" -> ModCategory.PERFORMANCE;
                        case "social" -> ModCategory.INFORMATION;
                        case "storage" -> ModCategory.STORAGE;
                        case "technology" -> ModCategory.TECHNOLOGY;
                        case "transportation" -> ModCategory.TRANSPORTATION;
                        case "worldgen" -> ModCategory.WORLD_GEN;
                        case "cursed" -> ModCategory.CURSED;
                        default -> null;
                    };

                    case RESOURCEPACK -> switch (s) {
                        case "128x" -> ResourcepackCategory.RES_128X;
                        case "256x" -> ResourcepackCategory.RES_256X;
                        case "512x+" -> ResourcepackCategory.RES_512X;
                        case "16x" -> ResourcepackCategory.RES_16X;
                        case "32x" -> ResourcepackCategory.RES_32X;
                        case "64x" -> ResourcepackCategory.RES_64X;
                        case "8x-" -> ResourcepackCategory.RES_8X;
                        case "48x" -> ResourcepackCategory.RES_48X;
                        case "animated" -> ResourcepackCategory.ANIMATED;
                        case "traditional", "audio", "blocks", "combat", "core-shaders",
                             "decoration", "entities","environment", "equipment", "gui",
                             "items", "locale", "models" -> ResourcepackCategory.TRADITIONAL;
                        case "realistic" -> ResourcepackCategory.REALISTIC;
                        case "simplistic" -> ResourcepackCategory.SIMPLISTIC;
                        case "themed" -> ResourcepackCategory.THEMED;
                        case "miscellaneous", "tweaks", "utility" -> ResourcepackCategory.MISCELLANEOUS;
                        case "fonts" -> ResourcepackCategory.FONT_PACKS;
                        case "modded" -> ResourcepackCategory.MOD_SUPPORT;
                        case "cursed" -> ResourcepackCategory.CURSED;
                        case "vanilla-like" -> ResourcepackCategory.VANILLA;
                        default -> null;
                    };

                    case SHADER -> switch (s) {
                        case "realistic", "atmosphere" -> ShaderCategory.REALISTIC;
                        case "fantasy" -> ShaderCategory.FANTASY;
                        case "shadows" -> ShaderCategory.SHADOWS;
                        case "screenshot", "potato" -> ShaderCategory.PERFORMANCE;
                        case "vanilla-like" -> ShaderCategory.VANILLA;
                        case "reflections", "pbr", "path-tracing", "foliage", "colored-lighting", "bloom" -> ShaderCategory.FEATURE;
                        case "medium" -> ShaderCategory.MEDIUM;
                        case "low" -> ShaderCategory.LOW;
                        case "semi_realistic" -> ShaderCategory.SEMI_REALISTIC;
                        case "high" -> ShaderCategory.HIGH;
                        case "cursed", "cartoon" -> ShaderCategory.CURSED;
                        default -> null;
                    };

                    case MODPACK -> switch (s) {
                        case "adventure" -> ModpackCategory.RPG;
                        case "challenging" -> ModpackCategory.HARDCORE;
                        case "combat" -> ModpackCategory.COMBAT;
                        case "kitchen-sink" -> ModCategory.MISCELLANEOUS;
                        case "lightweight" -> ModpackCategory.SMALL;
                        case "magic" -> ModpackCategory.MAGIC;
                        case "multiplayer" -> ModpackCategory.MULTIPLAYER;
                        case "optimization" -> ModpackCategory.VANILLA_PLUS;
                        case "quests" -> ModpackCategory.QUESTS;
                        case "technology" -> ModpackCategory.TECH;
                        default -> null;
                    };

                    default -> null;
                };
                if (t != null) set.add(t);
            }
            return set;
        }

        @Override
        public URL[] getScreenshots() {
            final JsonArray gallery = data.get("gallery").getAsJsonArray();
            final List<URL> urls = new ArrayList<>();
            for (JsonElement g : gallery) {
                try {
                    final String u = g.getAsJsonObject().get("url").getAsString();
                    if (u != null && !u.isEmpty()) {
                        urls.add(new URL(u));
                    }
                } catch (MalformedURLException ignored) {}
            }
            return urls.toArray(new URL[0]);
        }

        @Override @Unmodifiable
        public Collection<IntegrationFile> getFiles() {
            final List<IntegrationFile> fileList = new ArrayList<>();
            for (int i = 0; i < versions.size(); i++) {
                final JsonObject ver = versions.get(i).getAsJsonObject();
                final JsonArray fileArray = ver.get("files").getAsJsonArray();
                JsonObject pref = null;
                for (JsonElement f : fileArray) {
                    JsonObject fo = f.getAsJsonObject();
                    if (fo.get("primary").getAsBoolean()) {
                        pref = fo;
                        break;
                    }
                }
                if (pref == null && !fileArray.isEmpty()) {
                    pref = fileArray.get(0).getAsJsonObject();
                }
                if (pref == null) continue;
                final JsonObject pf = pref;
                final IntegrationWrapper parent = this;
                final IntegrationType type = getType();
                fileList.add(new IntegrationFile() {

                    private Collection<IntegrationFile> dependencies = null;

                    @Override
                    public IntegrationType getType() {
                        return type;
                    }

                    @Override
                    public Side getSide() {
                        if(type == PLUGIN) return SERVER;

                        final String s = data.get("server_side").getAsString();
                        final String c = data.get("client_side").getAsString();

                        Side side = ANY;
                        if(c.equals("unsupported") || c.equals("unknown")) side = SERVER;
                        else if(s.equals("unsupported") || s.equals("unknown")) side = CLIENT;

                        return side;
                    }

                    @Override
                    public String getChangelog() {
                        return ver.get("changelog").getAsString();
                    }

                    @Override
                    public String getFileName() {
                        return pf.get("filename").getAsString();
                    }

                    @Override
                    public URL getURL() {
                        try {
                            return new URL(pf.get("url").getAsString());
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }

                    @Override
                    public int getSize() {
                        return pf.get("size").getAsInt();
                    }

                    @Override
                    public Date getPublished() {
                        return Date.from(Instant.parse(ver.get("date_published").getAsString()));
                    }

                    @Override
                    public int getFileDownloads() {
                        return ver.get("downloads").getAsInt();
                    }

                    @Override
                    public ILoader[] getLoaders() {
                        final JsonArray loaders = ver.get("loaders").getAsJsonArray();
                        final List<ILoader> ll = new ArrayList<>();
                        for (int j = 0; j < loaders.size(); j++) {
                            final String l = loaders.get(j).getAsString().toUpperCase();
                            ILoader loader = null;

                            try {
                                loader = ModLoader.valueOf(l);
                            } catch (IllegalArgumentException ignored) {}

                            if (loader == null) {
                                try {
                                    loader = PluginLoader.valueOf(l);
                                } catch (IllegalArgumentException ignored) {}
                            }

                            if (loader == null) {
                                try {
                                    loader = ShaderLoader.valueOf(l);
                                } catch (IllegalArgumentException ignored) {}
                            }

                            if(loader == null && (l.equals("DATAPACK") || l.equals("MINECRAFT"))) loader = ILoader.ANY;

                            if(loader != null) ll.add(loader);
                        }
                        return ll.toArray(new ILoader[0]);
                    }

                    @Override
                    public String[] getVersions() {
                        final JsonArray vers = ver.get("game_versions").getAsJsonArray();
                        final String[] vs = new String[vers.size()];
                        for (int j = 0; j < vers.size(); j++) {
                            vs[j] = vers.get(j).getAsString();
                        }
                        return vs;
                    }

                    @Override
                    public Integration getParentIntegration() {
                        return parent;
                    }

                    @Override @Unmodifiable
                    public Collection<IntegrationFile> getDependencies() {
                        if (dependencies != null) {
                            return dependencies;
                        }
                        final JsonArray deps = ver.get("dependencies").getAsJsonArray();
                        final List<IntegrationFile> dependencies = new ArrayList<>();
                        for (JsonElement e : deps) {
                            final JsonObject d = e.getAsJsonObject();
                            if (d.get("dependency_type").getAsString().equals("required")) {
                                final String projid = d.get("project_id").getAsString();
                                final String vid = d.has("version_id") && !d.get("version_id").isJsonNull() ? d.get("version_id").getAsString() : null;
                                try {
                                    final ModrinthWrapper mod = new ModrinthWrapper(projid, key);
                                    final Collection<IntegrationFile> files = mod.getFiles();
                                    if (vid != null) {
                                        for (IntegrationFile mf : files) {
                                            if (mf.getId().equals(vid)) {
                                                dependencies.add(mf);
                                                break;
                                            }
                                        }
                                    } else {
                                        mod.getFiles().stream().filter(
                                                f -> f.getPossibleVersions().stream().anyMatch(
                                                        i -> getPossibleVersions().contains(i)
                                                )
                                        ).findFirst().ifPresent(dependencies::add);
                                    }
                                } catch (IOException ignored) {}
                            }
                        }
                        this.dependencies = Collections.unmodifiableCollection(dependencies);
                        return this.dependencies;
                    }

                    @Override
                    public String getId() {
                        return ver.get("id").getAsString();
                    }

                });
            }
            return Collections.unmodifiableCollection(fileList);
        }

        @Override
        public Platform getPlatform() {
            return MODRINTH;
        }
    }

    /**
     * {@code CurseforgeWrapper} is a wrapper class for CurseForge platform
     */
    private static final class CurseforgeWrapper extends IntegrationWrapper {
        private final JsonObject data;
        private final JsonArray versions;

        public CurseforgeWrapper(final String modid, final @Nullable String key) throws IOException {
            super(modid, key);
            this.data = getResponse("/mods/" + modid, key).get("data").getAsJsonObject();
            this.versions = getResponse("/mods/" + modid + "/files?pageSize=10000", key).get("data").getAsJsonArray();
        }

        @Override
        protected String connect(String endpoint, @Nullable String key) throws IOException {
            final URL url = new URL(CURSEFORGE.getUrl() + endpoint);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "ModdingLibrary/1.0");
            if(CURSEFORGE.isKeyRequired()) {
                if(key != null) {
                    con.setRequestProperty("x-api-key", key);
                } else {
                    throw new IOException("API key is required");
                }
            }
            final int code = con.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                throw new RuntimeException("Failed to fetch data: HTTP error code " + code);
            }
        }

        @Override
        public String getTitle() {
            return data.get("name").getAsString();
        }

        @Override
        public String getSlug() {
            return data.get("slug").getAsString();
        }

        /**
         * @return The descriptions of the integration with HTML tags.
         */
        @Override
        public String getFullDescription() {
            try {
                return getResponse("/mods/" + getId() + "/description", key).get("data").getAsString();
            } catch (IOException e) {
                return "";
            }
        }

        @Override
        public String getTeam() {
            return data.get("authors").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
        }

        @Override
        public String getId() {
            return String.valueOf(data.get("id").getAsInt());
        }

        /**
         * @return Always "Unknown" as CurseForge API does not provide license information
         */
        @Override @Deprecated
        public String getLicense() {
            return "Unknown";
        }

        /**
         * @return Always null because CurseForge API does not provide organisation information
         */
        @Override @Deprecated @Nullable
        public String getOrganisation() {
            return null;
        }

        @Override
        public Date getPublished() {
            return Date.from(Instant.parse(data.get("dateCreated").getAsString()));
        }

        @Override
        public Date getUpdated() {
            return Date.from(Instant.parse(data.get("dateModified").getAsString()));
        }

        @Override
        public Date getApproved() {
            return Date.from(Instant.parse(data.get("dateReleased").getAsString()));
        }

        @Override
        public IntegrationType getType() {
            return switch (data.get("classId").getAsInt()) {
                case 6 -> MOD;
                case 5 -> PLUGIN;
                case 12 -> RESOURCEPACK;
                case 17 -> WORLD;
                case 4546 -> CUSTOMIZATION;
                case 4471 -> MODPACK;
                case 4559 -> ADDON;
                case 6552 -> SHADER;
                case 6945 -> DATAPACK;
                default -> MOD;
            };
        }

        @Override
        public Status getStatus() {
            return switch (data.get("status").getAsInt()) {
                case 1 -> NEW;
                case 2 -> CHANGES_REQUIRED;
                case 3 -> UNDER_SOFT_REVIEW;
                case 4 -> APPROVED;
                case 5 -> REJECTED;
                case 6 -> CHANGES_MADE;
                case 7 -> INACTIVE;
                case 8 -> ABANDONED;
                case 9 -> DELETED;
                case 10 -> UNDER_REVIEW;
                default -> UNKNOWN;
            };
        }

        @Override
        public int getDownloads() {
            return data.get("downloadCount").getAsInt();
        }

        @Override
        public int getLikes() {
            return data.get("thumbsUpCount").getAsInt();
        }

        @Override @Nullable
        public URL getIcon() {
            try {
                final String i = data.get("logo").getAsJsonObject().get("url").getAsString();
                if (i == null || i.isEmpty()) return null;
                return new URL(i);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getIssues() {
            try {
                final String i = data.get("links").getAsJsonObject().get("issuesUrl").getAsString();
                if (i == null || i.isEmpty()) return null;
                return new URL(i);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getWiki() {
            try {
                final String w = data.get("links").getAsJsonObject().get("wikiUrl").getAsString();
                if (w == null || w.isEmpty()) return null;
                return new URL(w);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override @Nullable
        public URL getSource() {
            try {
                final String s = data.get("links").getAsJsonObject().get("sourceUrl").getAsString();
                if (s == null || s.isEmpty()) return null;
                return new URL(s);
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        /**
         * @return Always null because CurseForge API does not provide donation information
         */
        @Override
        public @Nullable URL getDonation() {
            return null;
        }

        @Override
        public Set<String> getAuthors() {
            try {
                final JsonArray authorsar = data.get("authors").getAsJsonArray();
                final Set<String> authors = new HashSet<>(authorsar.size());
                for (int i = 0; i < authorsar.size(); i++) {
                    authors.add(authorsar.get(i).getAsJsonObject().get("name").getAsString());
                }
                return authors;
            } catch (Exception e) {
                return Collections.emptySet();
            }
        }

        @Override
        public Set<ICategory> getCategories() {
            final JsonArray categories = data.get("categories").getAsJsonArray();
            final Set<ICategory> set = new HashSet<>();
            for (JsonElement c : categories) {
                final int clazz = c.getAsJsonObject().get("classId").getAsInt();
                final int id = c.getAsJsonObject().get("id").getAsInt();
                final ICategory t = switch (clazz) {
                    case 5 -> switch (id) {
                        case 124 -> PluginCategory.WORLD_EDITING_AND_MANAGEMENT;
                        case 128 -> PluginCategory.INFORMATIONAL;
                        case 115 -> PluginCategory.ADMIN_TOOLS;
                        case 133 -> PluginCategory.MISCELLANEOUS;
                        case 132 -> PluginCategory.ROLE_PLAYING;
                        case 116 -> PluginCategory.ANTI_GRIEFING_TOOLS;
                        case 122 -> PluginCategory.DEVELOPER_TOOLS;
                        case 127 -> PluginCategory.GENERAL;
                        case 125 -> PluginCategory.FIXES;
                        case 129 -> PluginCategory.MECHANICS;
                        case 131 -> PluginCategory.WORLD_GENERATORS;
                        case 130 -> PluginCategory.WEBSITE_ADMINISTRATION;
                        case 134 -> PluginCategory.TRANSPORTATION;
                        case 126 -> PluginCategory.FUN;
                        case 117 -> PluginCategory.CHAT_RELATED;
                        case 123 -> PluginCategory.ECONOMY;
                        case 4672 -> PluginCategory.TWITCH_INTEGRATION;
                        default -> null;
                    };

                    case 6 -> switch (id) {
                        case 436 -> ModCategory.FOOD;
                        case 408 -> ModCategory.ORES;
                        case 425 -> ModCategory.MISCELLANEOUS;
                        case 424 -> ModCategory.COSMETIC;
                        case 5299 -> ModCategory.EDUCATION;
                        case 413 -> ModCategory.PROCESSING;
                        case 423 -> ModCategory.INFORMATION;
                        case 416 -> ModCategory.FARMING;
                        case 412 -> ModCategory.TECHNOLOGY;
                        case 418 -> ModCategory.GENETICS;
                        case 409 -> ModCategory.STRUCTURES;
                        case 411 -> ModCategory.MOBS;
                        case 419 -> ModCategory.MAGIC;
                        case 426, 427, 432, 428, 429, 4545,
                             433, 4485, 430, 4773, 5314, 5232,
                             6145, 6484, 6954, 7669, 9049 -> ModCategory.ADDONS;
                        case 410 -> ModCategory.DIMENSIONS;
                        case 434 -> ModCategory.EQUIPMENT;
                        case 406 -> ModCategory.WORLD_GEN;
                        case 435 -> ModCategory.UTILITIES;
                        case 414 -> ModCategory.TRANSPORTATION;
                        case 417 -> ModCategory.ENERGY;
                        case 407 -> ModCategory.BIOMES;
                        case 422 -> ModCategory.RPG;
                        case 421 -> ModCategory.LIBRARY;
                        case 420 -> ModCategory.STORAGE;
                        case 4558 -> ModCategory.REDSTONE;
                        case 4843 -> ModCategory.AUTOMATION;
                        case 4671 -> ModCategory.TWITCH_INTEGRATION;
                        case 4906 -> ModCategory.MCREATOR;
                        case 6814 -> ModCategory.PERFORMANCE;
                        case 6821 -> ModCategory.BUG_FIXES;
                        case 9026 -> ModCategory.CREATIVE;
                        default -> null;
                    };

                    case 12 -> switch (id) {
                        case 400 -> ResourcepackCategory.REALISTIC;
                        case 399 -> ResourcepackCategory.STEAMPUNK;
                        case 403 -> ResourcepackCategory.TRADITIONAL;
                        case 398 -> ResourcepackCategory.RES_512X;
                        case 396 -> ResourcepackCategory.RES_128X;
                        case 397 -> ResourcepackCategory.RES_256X;
                        case 402 -> ResourcepackCategory.MEDIEVAL;
                        case 395 -> ResourcepackCategory.RES_64X;
                        case 405 -> ResourcepackCategory.MISCELLANEOUS;
                        case 394 -> ResourcepackCategory.RES_32X;
                        case 393 -> ResourcepackCategory.RES_16X;
                        case 404 -> ResourcepackCategory.ANIMATED;
                        case 401 -> ResourcepackCategory.MODERN;
                        case 4465 -> ResourcepackCategory.MOD_SUPPORT;
                        case 5193 -> ResourcepackCategory.DATA_PACKS;
                        case 5244 -> ResourcepackCategory.FONT_PACKS;
                        default -> null;
                    };

                    case 17 -> switch (id) {
                        case 251 -> WorldCategory.PARKOUR;
                        case 253 -> WorldCategory.SURVIVAL;
                        case 249 -> WorldCategory.CREATION;
                        case 250 -> WorldCategory.GAME_MAP;
                        case 248 -> WorldCategory.ADVENTURE;
                        case 4464 -> WorldCategory.MODDED_WORLD;
                        case 252 -> WorldCategory.PUZZLE;
                        default -> null;
                    };

                    case 4471 -> switch (id) {
                        case 4475 -> ModpackCategory.RPG;
                        case 4487 -> ModpackCategory.FTB;
                        case 4478 -> ModpackCategory.QUESTS;
                        case 4481 -> ModpackCategory.SMALL;
                        case 4483 -> ModpackCategory.COMBAT;
                        case 4472 -> ModpackCategory.TECH;
                        case 4474 -> ModpackCategory.SCI_FI;
                        case 4479 -> ModpackCategory.HARDCORE;
                        case 4484 -> ModpackCategory.MULTIPLAYER;
                        case 4477 -> ModpackCategory.MINI_GAME;
                        case 4482 -> ModpackCategory.EXTRA_LARGE;
                        case 4473 -> ModpackCategory.MAGIC;
                        case 4736 -> ModpackCategory.SKYBLOCK;
                        case 4480 -> ModpackCategory.MAP_BASED;
                        case 4476 -> ModpackCategory.EXPLORATION;
                        case 5128 -> ModpackCategory.VANILLA_PLUS;
                        case 4471 -> ModpackCategory.HORROR;
                        default -> null;
                    };

                    case 4546 -> switch (id) {
                        case 4551 -> CustomizationCategory.HARDCORE_QUESTING_MODE;
                        case 4549 -> CustomizationCategory.GUIDEBOOK;
                        case 4554 -> CustomizationCategory.RECIPES;
                        case 4556 -> CustomizationCategory.PROGRESSION;
                        case 4550 -> CustomizationCategory.QUESTS;
                        case 4752 -> CustomizationCategory.BUILDING_GADGETS;
                        case 4548 -> CustomizationCategory.LUCKY_BLOCKS;
                        case 4547 -> CustomizationCategory.CONFIGURATION;
                        case 4555 -> CustomizationCategory.WORLD_GEN;
                        case 4552 -> CustomizationCategory.SCRIPTS;
                        case 5186 -> CustomizationCategory.FANCY_MENU;
                        default -> null;
                    };

                    case 4559 -> switch (id) {
                        case 4561 -> AddonCategory.RESOURCE_PACKS;
                        case 4562 -> AddonCategory.SCENARIOS;
                        case 4560 -> AddonCategory.WORLDS;
                        default -> null;
                    };

                    case 6945 -> switch (id) {
                        case 6952 -> DatapackCategory.MAGIC;
                        case 6945 -> DatapackCategory.MISCELLANEOUS;
                        case 6554 -> DatapackCategory.FANTASY;
                        case 4465 -> DatapackCategory.MOD_SUPPORT;
                        case 412 -> DatapackCategory.TECH;
                        case 421 -> DatapackCategory.LIBRARY;
                        case 5191 -> DatapackCategory.UTILITY;
                        case 422 -> DatapackCategory.ADVENTURE;
                        default -> null;
                    };

                    case 6552 -> switch (id) {
                        case 6555 -> ShaderCategory.VANILLA;
                        case 6554 -> ShaderCategory.FANTASY;
                        case 6553 -> ShaderCategory.REALISTIC;
                        default -> null;
                    };

                    default -> null;
                };
                if(t != null) set.add(t);
            }
            return set;
        }

        @Override
        public URL[] getScreenshots() {
            final JsonArray screenshots = data.get("screenshots").getAsJsonArray();
            final List<URL> urls = new ArrayList<>();
            for (JsonElement sc : screenshots) {
                try {
                    final String u = sc.getAsJsonObject().get("url").getAsString();
                    if (u != null && !u.isEmpty()) {
                        urls.add(new URL(u));
                    }
                } catch (MalformedURLException ignored) {}
            }
            return urls.toArray(new URL[0]);
        }

        @Override @Unmodifiable
        public Collection<IntegrationFile> getFiles() {
            final List<IntegrationFile> list = new ArrayList<>();
            final String modid = getId();
            final @Nullable String key = this.key;
            final IntegrationWrapper parent = this;
            final IntegrationType type = getType();
            for (int i = 0; i < versions.size(); i++) {
                final JsonObject object = versions.get(i).getAsJsonObject();
                list.add(new IntegrationFile() {

                    private Collection<IntegrationFile> dependencies = null;

                    @Override
                    public IntegrationType getType() {
                        return type;
                    }

                    @Override
                    public Side getSide() {
                        if(type == PLUGIN) return SERVER;

                        final JsonArray sgv = object.get("sortableGameVersions").getAsJsonArray();
                        final List<Side> sides = new ArrayList<>();
                        for (JsonElement el : sgv) {
                            final JsonObject obj = el.getAsJsonObject();
                            final String type = obj.get("gameVersionName").getAsString().toUpperCase();

                            try {
                                sides.add(Side.valueOf(type));
                            } catch (IllegalArgumentException ignored) {}
                        }

                        if ((sides.contains(SERVER) && sides.contains(CLIENT)) || sides.isEmpty()) return ANY;
                        else return sides.getFirst();
                    }

                    /**
                     * @return The changelog of the integration file with HTML tags.
                     */
                    @Override
                    public String getChangelog() {
                        String changelog;
                        try {
                            final JsonObject json = getResponse("/mods/" + modid + "/files/" + object.get("id").getAsString() + "/changelog", key);
                            changelog = json.get("data").getAsString();
                            return changelog;
                        } catch (Exception e) {
                            return "";
                        }
                    }

                    @Override
                    public String getFileName() {
                        return object.get("fileName").getAsString();
                    }

                    @Override @Nullable
                    public URL getURL() {
                        try {
                            return new URL(object.get("downloadUrl").getAsString());
                        } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                            return null;
                        }
                    }

                    @Override
                    public int getSize() {
                        return object.get("fileLength").getAsInt();
                    }

                    @Override
                    public Date getPublished() {
                        return Date.from(Instant.parse(object.get("fileDate").getAsString()));
                    }

                    @Override
                    public int getFileDownloads() {
                        return object.get("downloadCount").getAsInt();
                    }

                    @Override
                    public ILoader[] getLoaders() {
                        final JsonArray sgv = object.get("sortableGameVersions").getAsJsonArray();
                        final List<ILoader> ll = new ArrayList<>();
                        for (JsonElement el : sgv) {
                            final JsonObject obj = el.getAsJsonObject();
                            final String l = obj.get("gameVersionName").getAsString().toUpperCase();
                            if(obj.get("gameVersionPadded").getAsString().equals("0")) {
                                ILoader loader = null;

                                try {
                                    loader = ModLoader.valueOf(l);
                                } catch (IllegalArgumentException ignored) {}

                                if (loader == null) {
                                    try {
                                        loader = PluginLoader.valueOf(l);
                                    } catch (IllegalArgumentException ignored) {}
                                }

                                if (loader == null) {
                                    try {
                                        loader = ShaderLoader.valueOf(l);
                                    } catch (IllegalArgumentException ignored) {}
                                }

                                if(loader != null) ll.add(loader);
                            }
                        }

                        ILoader loader = null;

                        if (
                                type == DATAPACK ||
                                type == RESOURCEPACK ||
                                type == WORLD ||
                                type == CUSTOMIZATION ||
                                type == ADDON
                        ) loader = ILoader.ANY;
                        else if (type == PLUGIN) loader = PluginLoader.BUKKIT;

                        if(loader != null) ll.add(loader);

                        if(ll.isEmpty()) ll.add(getPossibleLoaders().toArray(new ILoader[0])[0]);

                        return ll.toArray(new ILoader[0]);
                    }

                    @Override
                    public String[] getVersions() {
                        final JsonArray sgv = object.get("sortableGameVersions").getAsJsonArray();
                        final List<String> ll = new ArrayList<>();
                        for (JsonElement el : sgv) {
                            final JsonObject obj = el.getAsJsonObject();
                            final String type = obj.get("gameVersionName").getAsString().toLowerCase();
                            if(!obj.get("gameVersionPadded").getAsString().equals("0") && type.equals(obj.get("gameVersionName").getAsString())) {
                                ll.add(type);
                            }
                        }
                        return ll.toArray(new String[0]);
                    }

                    @Override
                    public Integration getParentIntegration() {
                        return parent;
                    }

                    @Override @Unmodifiable
                    public Collection<IntegrationFile> getDependencies() {
                        if (dependencies != null) {
                            return dependencies;
                        }
                        final List<IntegrationFile> dependencies = new ArrayList<>();
                        for (JsonElement e : object.get("dependencies").getAsJsonArray()) {
                            final JsonObject d = e.getAsJsonObject();
                            if (d.get("relationType").getAsInt() == 3) {
                                final String id = d.get("modId").getAsString();
                                try {
                                    final CurseforgeWrapper mod = new CurseforgeWrapper(id, key);
                                    mod.getFiles().stream().filter(
                                            f -> f.getPossibleVersions().stream().anyMatch(
                                                    i -> getPossibleVersions().contains(i)
                                            )
                                    ).findFirst().ifPresent(dependencies::add);
                                } catch (IOException ignored) {}
                            }
                        }
                        this.dependencies = Collections.unmodifiableCollection(dependencies);
                        return this.dependencies;
                    }

                    @Override
                    public String getId() {
                        return object.get("id").getAsString();
                    }

                });
            }
            return Collections.unmodifiableCollection(list);
        }

        @Override
        public Platform getPlatform() {
            return CURSEFORGE;
        }
    }

    /**
     * {@code SpigetWrapper} is a wrapper class for Spiget platform
     */
    private static final class SpigetWrapper extends IntegrationWrapper {
        private final JsonObject data;
        private final JsonArray versions;
        private final String url = "https://www.spigotmc.org/";

        public SpigetWrapper(final String modid, final @Nullable String key) throws IOException {
            super(modid, key);
            this.data = getResponse("/resources/" + modid, key);
            this.versions = getResponseArray("/resources/" + modid + "/versions?size=10000", key);
        }

        @Override
        protected JsonObject getResponse(final String endpoint, final @Nullable String key) throws IOException {
            final JsonElement json = JsonParser.parseString(connect(endpoint, key));
            if (!json.isJsonObject()) {
                throw new JsonSyntaxException("Expected JsonObject but got " + json.getClass().getSimpleName() + ": " + json);
            }
            return json.getAsJsonObject();
        }

        @Override
        protected JsonArray getResponseArray(final String endpoint, final @Nullable String key) throws IOException {
            final JsonElement json = JsonParser.parseString(connect(endpoint, key));
            if (!json.isJsonArray()) {
                throw new JsonSyntaxException("Expected JsonArray but got " + json.getClass().getSimpleName() + ": " + json);
            }
            return json.getAsJsonArray();
        }

        @Override
        protected String connect(final String endpoint, final @Nullable String key) throws IOException {
            final URL url = new URL(SPIGET.getUrl() + endpoint);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "ModdingLibrary/1.0");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.setConnectTimeout(20000);
            con.setReadTimeout(20000);
            if(SPIGET.isKeyRequired()) {
                if(key != null) {
                    con.setRequestProperty("x-api-key", key);
                } else {
                    throw new IOException("API key is required");
                }
            }
            try (final InputStream in = getPossiblyDecompressedStream(con);
                 final ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                final byte[] chunk = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }

                final String response = buffer.toString(StandardCharsets.UTF_8);
                con.disconnect();
                return response;
            } catch (IOException e) {
                con.disconnect();
                throw e;
            }
        }

        private InputStream getPossiblyDecompressedStream(HttpURLConnection connection) throws IOException {
            final InputStream inputStream = connection.getInputStream();
            final String encoding = connection.getContentEncoding();
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                return new GZIPInputStream(inputStream);
            }
            return inputStream;
        }

        @Override
        public String getTitle() {
            return data.get("name").getAsString();
        }

        @Override
        public String getSlug() {
            return data.get("file").getAsJsonObject().get("url").getAsString().split("/")[1].split("\\.")[0];
        }

        @Override
        public String getFullDescription() {
            return Base64.getDecoder().decode(data.get("description").getAsString()).toString();
        }

        @Override
        public String getId() {
            return data.get("id").getAsString();
        }

        /**
         * @return Always "Unknown" as the Spiget API does not provide license information
         */
        @Override @Deprecated
        public String getLicense() {
            return "Unknown";
        }

        /**
         * @return Always null as the Spiget API does not provide organisation information
         */
        @Override @Nullable @Deprecated
        public String getOrganisation() {
            return null;
        }

        /**
         * @return Always null as the Spiget API does not provide team information
         */
        @Override @Nullable @Deprecated
        public String getTeam() {
            return null;
        }

        @Override
        public IntegrationType getType() {
            return PLUGIN;
        }

        public Version[] getTested() {
            return data.getAsJsonArray("testedVersions").asList().stream()
                    .map(JsonElement::getAsString)
                    .map(i -> new Version(ILoader.ANY, i)).toArray(Version[]::new);
        }

        @Override
        public Set<String> getAuthors() {
            final Set<String> authors = new HashSet<>();
            if(data.has("contributors")) {
                final String[] contributors = data.get("contributors").getAsString().split(", ");
                authors.addAll(Arrays.asList(contributors));
            }
            try {
                authors.add(
                        getResponse("/authors/" + data.get("author").getAsJsonObject().get("id").getAsString(), this.key)
                                .get("name").getAsString()
                );
            } catch (IOException ignored) {}

            return authors;
        }

        @Override
        public boolean isPremium() {
            return data.get("premium").getAsBoolean();
        }

        @Override
        public Date getPublished() {
            return Date.from(Instant.ofEpochMilli(data.get("releaseDate").getAsLong()));
        }

        @Override
        public Date getUpdated() {
            return Date.from(Instant.ofEpochMilli(data.get("updateDate").getAsLong()));
        }

        @Override
        public Date getApproved() {
            return getPublished();
        }

        @Override
        public Set<ICategory> getCategories() {
            PluginCategory c;
            switch (data.get("category").getAsJsonObject().get("id").getAsInt()) {
                case 21, 19, 28, 2, 3, 4, 20 -> c = PluginCategory.GENERAL;
                case 9, 26, 25, 15, 12, 7 -> c = PluginCategory.DEVELOPER_TOOLS;
                case 6, 11, 14 -> c = PluginCategory.CHAT_RELATED;
                case 16, 13, 8 -> c = PluginCategory.MISCELLANEOUS;
                case 5, 10 -> c = PluginCategory.TRANSPORTATION;
                case 18 -> c = PluginCategory.WORLD_EDITING_AND_MANAGEMENT;
                case 27 -> c = PluginCategory.WEBSITE_ADMINISTRATION;
                case 29 -> c = PluginCategory.WORLD_GENERATORS;
                case 24 -> c = PluginCategory.ROLE_PLAYING;
                case 22 -> c = PluginCategory.MECHANICS;
                case 23 -> c = PluginCategory.ECONOMY;
                case 17 -> c = PluginCategory.FUN;
                default -> {
                    return Collections.emptySet();
                }
            }
            return Set.of(c);
        }

        @Override
        public Status getStatus() {
            return UNKNOWN;
        }

        @Override
        public int getDownloads() {
            return data.get("downloads").getAsInt();
        }

        @Override
        public int getLikes() {
            return data.get("likes").getAsInt();
        }

        @Override
        public @Nullable URL getIcon() {
            try {
                return new URL(url + data.get("icon").getAsJsonObject().get("url").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override
        public @Nullable URL getDonation() {
            try {
                return new URL(data.get("donationLink").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        /**
         * @return Always null as the Spiget API does not provide issues URL of the project
         */
        @Override @Nullable @Deprecated
        public URL getIssues() {
            return null;
        }

        @Override
        public @Nullable URL getWiki() {
            try {
                return new URL(data.get("documentation").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override
        public @Nullable URL getSource() {
            try {
                return new URL(data.get("sourceCodeLink").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        /**
         * @return Always empty array as the Spiget API does not provide screenshots
         */
        @Override @Deprecated
        public URL[] getScreenshots() {
            return new URL[0];
        }

        @Override @Unmodifiable
        public Collection<IntegrationFile> getFiles() {
            final List<IntegrationFile> files = new ArrayList<>();
            final IntegrationWrapper parent = this;
            final Version[] t = getTested();
            final String drl = url + data.get("file").getAsJsonObject().get("url").getAsString().split("=")[0] + "=";
            for (int i = 0; i < versions.size(); i++) {
                final JsonObject object = versions.get(i).getAsJsonObject();
                files.add(new IntegrationFile() {

                    /**
                     * @return Always empty String as Spiget API does not provide changelog of the files
                     */
                    @Override @Deprecated
                    public String getChangelog() {
                        return "";
                    }

                    @Override
                    public IntegrationType getType() {
                        return PLUGIN;
                    }

                    @Override
                    public Side getSide() {
                        return SERVER;
                    }

                    @Override
                    public String getFileName() {
                        return object.get("name").getAsString() + ".jar";
                    }

                    @Override
                    public URL getURL() {
                        try {
                            return new URL(drl + object.get("id").getAsString());
                        } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                            return null;
                        }
                    }

                    /**
                     * @return Always 1 because Spiget API does not provide information about file size
                     */
                    @Override @Deprecated
                    public int getSize() {
                        return 1;
                    }

                    @Override
                    public Date getPublished() {
                        return Date.from(Instant.ofEpochMilli(data.get("releaseDate").getAsLong()));
                    }

                    @Override
                    public int getFileDownloads() {
                        return object.get("downloads").getAsInt();
                    }

                    @Override
                    public PluginLoader[] getLoaders() {
                        return new PluginLoader[]{PluginLoader.SPIGOT, PluginLoader.PAPER};
                    }

                    @Override
                    public String[] getVersions() {
                        return Arrays.stream(t).map(Version::version).toArray(String[]::new);
                    }

                    @Override
                    public Integration getParentIntegration() {
                        return parent;
                    }

                    @Override @Unmodifiable
                    public Collection<IntegrationFile> getDependencies() {
                        return Collections.emptyList();
                    }

                    @Override
                    public String getId() {
                        return object.get("id").getAsString();
                    }

                });
            }
            return Collections.unmodifiableCollection(files);
        }

        @Override
        public Platform getPlatform() {
            return SPIGET;
        }
    }
}