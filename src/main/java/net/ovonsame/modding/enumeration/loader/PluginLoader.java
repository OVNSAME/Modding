package net.ovonsame.modding.enumeration.loader;

import net.ovonsame.modding.interfaces.ILoader;

import java.time.Instant;
import java.util.Date;

/**
 * Enumeration {@code PluginLoader} is a subclass of {@code ILoader} and represents all {@code IntegrationType.PLUGIN} loaders ever made
 */
public enum PluginLoader implements ILoader {
    BUKKIT(Date.from(Instant.parse("2011-01-06T00:00:00Z"))),
    SPIGOT(Date.from(Instant.parse("2012-10-22T00:00:00Z"))),
    PAPER(Date.from(Instant.parse("2015-09-21T00:00:00Z"))),
    PURPUR(Date.from(Instant.parse("2020-02-13T00:00:00Z"))),
    VELOCITY(Date.from(Instant.parse("2019-06-09T00:00:00Z"))),
    FOLIA(Date.from(Instant.parse("2022-12-23T00:00:00Z"))),
    WATERFALL(Date.from(Instant.parse("2016-06-26T00:00:00Z"))),
    SPONGE(Date.from(Instant.parse("2014-09-07T00:00:00Z"))),
    BUNGEECORD(Date.from(Instant.parse("2012-05-26T00:00:00Z")));

    private final Date created;

    PluginLoader(Date date) {
        this.created = date;
    }

    @Override
    public Date getCreated() {
        return created;
    }
}
