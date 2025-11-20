package net.ovonsame.modding.enumeration.loader;

import net.ovonsame.modding.interfaces.ILoader;

import java.time.Instant;
import java.util.Date;

/**
 * Enumeration {@code ModLoader} is a subclass of {@code ILoader} and represents all {@code IntegrationType.MOD} loaders
 */
public enum ModLoader implements ILoader {
    FORGE(Date.from(Instant.parse("2011-02-19T00:00:00Z"))),
    CAULDRON(Date.from(Instant.parse("2014-02-01T00:00:00Z"))),
    LITE_LOADER(Date.from(Instant.parse("2012-08-09T00:00:00Z"))),
    FABRIC(Date.from(Instant.parse("2018-12-10T00:00:00Z"))),
    QUILT(Date.from(Instant.parse("2021-03-20T00:00:00Z"))),
    NEOFORGE(Date.from(Instant.parse("2023-06-06T00:00:00Z")));

    private final Date created;

    ModLoader(Date date) {
        this.created = date;
    }

    @Override
    public final Date getCreated() {
        return created;
    }
}