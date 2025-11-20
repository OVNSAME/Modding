package net.ovonsame.modding.enumeration.loader;

import net.ovonsame.modding.interfaces.ILoader;

import java.time.Instant;
import java.util.Date;

/**
 * Enumeration {@code ShaderLoader} is a subclass of {@code ILoader} and represents all {@code IntegrationType.SHADER} loaders including {@code VANILLA} as standart
 */
public enum ShaderLoader implements ILoader {
    VANILLA(ILoader.ANY.getCreated()),
    IRIS(Date.from(Instant.parse("2021-07-01T00:00:00Z"))),
    OPTIFINE(Date.from(Instant.parse("2011-04-08T00:00:00Z"))),
    CANVAS(Date.from(Instant.parse("2011-04-08T00:00:00Z"))); // TODO

    private final Date created;

    ShaderLoader(Date date) {
        this.created = date;
    }

    @Override
    public final Date getCreated() {
        return created;
    }
}
