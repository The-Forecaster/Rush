package trans.rights.event.bus;

/**
 * Basic structure for an event dispatcher
 *
 * @author Austin
 */
public interface EventBus {

    /**
     * Adds the Subscriber to the registry
     *
     * @param subscriber event Subscriber instance
     */
    void register(Object subscriber);

    /**
     * Removes the Subscriber from the registry
     *
     * @param subscriber event subscriber instance
     */
    void unregister(Object subscriber);

    /**
     * Check if an object is currently in the registry
     *
     * @return if the object is in the registry
     */
    boolean isRegistered(Object subscriber);

    /**
     * Post an event to be processed by the subscribed methods or listener objects
     *
     * @param <T> event type
     * @param event object to post
     *
     * @return the event you passed
     */
    <T> T dispatch(T event);
}
