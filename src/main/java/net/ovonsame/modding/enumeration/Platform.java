package net.ovonsame.modding.enumeration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Enumeration {@code Platform} represents all supported and the most popular platforms for posting Minecraft integrations
 */
public enum Platform {
    CURSEFORGE("https://api.curseforge.com/v1", true),
    MODRINTH("https://api.modrinth.com/v2", false),
    SPIGET("https://api.spiget.org/v2", false);

    private final String url;
    private final boolean key;

    Platform(String url, boolean key) {
        this.url = url;
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public boolean isKeyRequired() {
        return key;
    }

    private static final Gson GSON = new Gson();

    private static InputStream getPossiblyDecompressedStream(HttpURLConnection connection) throws IOException {
        final InputStream inputStream = connection.getInputStream();
        final String encoding = connection.getContentEncoding();
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            return new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    private String connect(final String endpoint, @Nullable final String key) throws IOException {
        final URL url = new URL(getUrl() + endpoint);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "ModdingLibrary/1.0");

        con.setConnectTimeout(20000);
        con.setReadTimeout(20000);

        if(isKeyRequired()) {
            if(key != null) {
                con.setRequestProperty("x-api-key", key);
            } else {
                throw new IOException("API key is required");
            }
        }

        if(this != SPIGET) {
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
        } else {
            con.setRequestProperty("Accept-Encoding", "gzip");

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
    }

    public final JsonObject getResponse(final String endpoint, final @Nullable String key) throws IOException {
        return GSON.fromJson(connect(endpoint, key), JsonObject.class);
    }

    public final JsonArray getResponseArray(final String endpoint, final @Nullable String key) throws IOException {
        return GSON.fromJson(connect(endpoint, key), JsonArray.class);
    }

    /**
     * The main abstract wrapper class for all platforms wrappers.
     */
    public static abstract class Wrapper {
        protected final String identifier;
        protected final @Nullable String key;

        public Wrapper(final String identifier, final @Nullable String key) {
            this.identifier = identifier;
            this.key = key;
        }
    }
}
