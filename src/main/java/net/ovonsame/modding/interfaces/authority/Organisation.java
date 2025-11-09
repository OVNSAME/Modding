package net.ovonsame.modding.interfaces.authority;

/**
 * Interface {@code Organisation} is a subclass of {@code Team} and represents an organization which has an owner
 */
public interface Organisation extends Team {
    /**
     * @return The owner of the organization
     */
    Author getOwner();
}
