package net.ovonsame.modding.interfaces;

import java.time.Instant;
import java.util.Date;

/**
 * Interface {@code ILoader} is a representation of a Minecraft loader.
 */
public interface ILoader {
    /**
     * @return creation date of the loader which is used to find possible loaders of integration if it does not have any loaders.
     */
    Date getCreated();

    /**
     * Loader {@code ILoader.ANY} is used for some of the integration types as they do not have loaders by default.
     */
    ILoader ANY = new ILoader() {
        @Override
        public String toString() { return "ANY"; }

        /**
         * @return Minecraft creation date
         */
        @Override
        public Date getCreated() { return Date.from(Instant.parse("2009-05-17T00:00:00Z")); }
    };
}
