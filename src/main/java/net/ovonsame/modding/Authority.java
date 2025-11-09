package net.ovonsame.modding;

import com.google.gson.JsonObject;
import net.ovonsame.modding.enumeration.Platform;
import net.ovonsame.modding.interfaces.authority.Author;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

import static net.ovonsame.modding.enumeration.Platform.*;

/**
 * Class {@code Authority} contains private wrapper classes for each supported integration platform which implements the {@code Author} interface
 * @see Wrapper
 * @see Author
 */
public final class Authority {

    /**
     * With this method you can get authors registered on a platform.
     * @param platform Platform to get author from
     * @param identifier Identifier of the author
     * @param key Optional key which is required for some platforms
     * @return Author from the specified platform with the specified identifier
     * @throws IOException If the author is not found, key is not provided or is invalid and the platform requires it, if some errors occurs while connecting with the platform
     * @see Author
     * @see Wrapper
     * @see Platform
     */
    public static Author getAuthor(final Platform platform, final String identifier, final @Nullable String key) throws IOException {
        return switch (platform) {
            case MODRINTH -> new ModrinthWrapper(identifier, key);
            case CURSEFORGE -> new CurseforgeWrapper(identifier, key);
            case SPIGET -> new SpigetWrapper(identifier, key);
        };
    }

    private static final class ModrinthWrapper extends Wrapper implements Author {
        private final JsonObject data;

        public ModrinthWrapper(final String identifier, final @Nullable String key) throws IOException {
            super(identifier, key);
            data = MODRINTH.getResponse("/user/" + identifier, key);
        }

        @Override
        public @Nullable URL getAvatar() {
            try {
                return new URL(data.get("avatar_url").getAsString());
            } catch (MalformedURLException e) {
                return null;
            }
        }

        @Override
        public String getId() {
            return data.get("id").getAsString();
        }

        @Override
        public String getName() {
            return data.has("name") && !data.get("name").isJsonNull() ? data.get("name").getAsString() : data.get("username").getAsString();
        }

        @Override
        public Date getRegistered() {
            return Date.from(Instant.parse(data.get("created").getAsString()));
        }

        @Override
        public Platform getPlatform() {
            return MODRINTH;
        }
    }

    private static final class CurseforgeWrapper extends Wrapper implements Author {
        private final JsonObject data;

        public CurseforgeWrapper(final String identifier, final @Nullable String key) throws IOException {
            super(identifier, key);
            data = CURSEFORGE.getResponse("/users/" + identifier, key).get("data").getAsJsonObject();
        }

        @Override
        public @Nullable URL getAvatar() {
            try {
                return new URL(data.get("avatarUrl").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override
        public String getId() {
            return data.get("id").getAsString();
        }

        @Override
        public String getName() {
            return data.get("displayName").getAsString();
        }

        @Override
        public Date getRegistered() {
            return Date.from(Instant.parse(data.get("dateCreated").getAsString()));
        }

        @Override
        public Platform getPlatform() {
            return CURSEFORGE;
        }
    }

    private static final class SpigetWrapper extends Wrapper implements Author {
        private final JsonObject data;

        public SpigetWrapper(final String identifier, final @Nullable String key) throws IOException {
            super(identifier, key);
            data = SPIGET.getResponse("/authors/" + identifier, key);
        }

        @Override
        public @Nullable URL getAvatar() {
            try {
                return new URL(data.get("icon").getAsJsonObject().get("url").getAsString());
            } catch (MalformedURLException | UnsupportedOperationException | NullPointerException e) {
                return null;
            }
        }

        @Override
        public String getId() {
            return data.get("id").getAsString();
        }

        @Override
        public String getName() {
            return data.get("name").getAsString();
        }

        /**
         * @return Always null as Spiget does not provide date of registration
         */
        @Override @Deprecated @Nullable
        public Date getRegistered() {
            return null;
        }

        @Override
        public Platform getPlatform() {
            return SPIGET;
        }
    }
}
