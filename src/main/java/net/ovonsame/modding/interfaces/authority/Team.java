package net.ovonsame.modding.interfaces.authority;

/**
 * {@code Team} interface is an @{link Iterable} of {@link Author} and is a named group of authors
 */
public interface Team extends Iterable<Author> {
    /**
     * @return The name of the team
     */
    String getName();
}
