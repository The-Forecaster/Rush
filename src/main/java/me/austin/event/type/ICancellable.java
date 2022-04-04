package trans.rights.event.type;

/**
 * Event structure for a cancellable event
 * by default the event should be uncancelled and be cancelled by calling
 * setCancelled(true)
 * 
 * @author Austin
 */
public interface ICancellable {
    /**
     * Checks if the object is cancelled
     * 
     * @return if the event has been cancelled
     */
    boolean isCancelled();

    /**
     * Changes if the object is cancelled
     * 
     * @param cancelled the value you want to set the object's cancelled status to
     */
    void setCancelled(boolean cancelled);
}
