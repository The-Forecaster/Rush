package me.austin.rush

/**
 * Basic structure for an event dispatcher.
 *
 * @author Austin
 * @since 2022
 */
interface EventBus {
    /**
     * Adds the listener into the registry.
     *
     * @param listener Instance of [Listener] to subscribe.
     */
    fun subscribe(listener: Listener)

    /**
     * Adds all [Listener] objects to the registry.
     *
     * @param listeners All [Listener] objects you want to be added.
     */
    fun subscribeAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.subscribe(listener)
        }
    }

    /**
     * Adds all [Listener] objects in an [Iterable] to the registry.
     *
     * @param listeners The [Iterable] of [Listener] objects you want to be added.
     */
    fun subscribeAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.subscribe(listener)
        }
    }

    /**
     * Removes the listener from the registry.
     *
     * @param listener [Listener] object to be removed.
     */
    fun unsubscribe(listener: Listener)

    /**
     * Removes all [Listener] objects from the registry.
     *
     * @param listeners [Listener] objects you want to be removed.
     * @see unsubscribe
     */
    fun unsubscribeAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.unsubscribe(listener)
        }
    }

    /**
     * Removes all [Listener] objects in an [Iterable] from the registry.
     *
     * @param listeners [Iterable] of [Listener] objects you want to be removed.
     * @see unsubscribe
     */
    fun unsubscribeAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.unsubscribe(listener)
        }
    }

    /**
     * Post an event to be processed by the listener objects.
     *
     * @param T Event type.
     * @param event Instance of [T] to post.
     */
    fun <T : Any> post(event: T): T

    /**
     * Dispatches an event that is cancellable.
     * When the event is cancelled it will not be posted to any listeners after.
     *
     * @param T The type of the [event] posted.
     * @param event The event which will be posted.
     * @return [event].
     */
    fun <T : Cancellable> post(event: T): T
}