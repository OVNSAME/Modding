package net.ovonsame.modding.enumeration;

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
}
