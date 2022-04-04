package me.austin.event.type;

/**
 * Default implementation of {@link ICancellable}
 * 
 * @author Austin
 */
public abstract class Cancellable implements ICancellable {

    /**
     * This stores whether the event is cancelled or not
     */
    private boolean cancelled = false;

    @Override
    public final boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
