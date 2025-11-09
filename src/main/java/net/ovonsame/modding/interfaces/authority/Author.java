package net.ovonsame.modding.interfaces.authority;

import net.ovonsame.modding.enumeration.Platform;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Date;

/**
 * Interface {@code Author} represents a person registered on a platform who has created a Minecraft integration
 */
public interface Author {
    /**
     * @return Nullable {@code URL} of the author's avatar
     */
    @Nullable URL getAvatar();

    /**
     * @return An identifier of the author
     */
    String getId();

    /**
     * @return A name of the author
     */
    String getName();

    /**
     * @return A date when the author was registered
     */
    Date getRegistered();

    /**
     * @return The platform on which the author is registered
     */
    Platform getPlatform();
}
