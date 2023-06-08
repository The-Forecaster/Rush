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
    fun register(listener: Listener)

    /**
     * Adds all [Listener] objects to the registry.
     *
     * @param listeners All [Listener] objects you want to be added.
     */
    fun registerAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.register(listener)
        }
    }

    /**
     * Adds all [Listener] objects in an [Iterable] to the registry.
     *
     * @param listeners The [Iterable] of [Listener] objects you want to be added.
     */
    fun registerAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.register(listener)
        }
    }

    /**
     * Removes the listener from the registry.
     *
     * @param listener [Listener] object to be removed.
     */
    fun unregister(listener: Listener)

    /**
     * Removes all [Listener] objects from the registry.
     *
     * @param listeners [Listener] objects you want to be removed.
     * @see unregister
     */
    fun unregisterAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.unregister(listener)
        }
    }

    /**
     * Removes all [Listener] objects in an [Iterable] from the registry.
     *
     * @param listeners [Iterable] of [Listener] objects you want to be removed.
     * @see unregister
     */
    fun unregisterAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.unregister(listener)
        }
    }

    /**
     * Adds all [EventHandler] annotated [Listener] objects into the registry.
     *
     * @param subscriber Object you want to be searched for listeners to be added to the registry.
     */
    fun register(subscriber: Any)

    /**
     * Adds all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want to be added to the registry.
     */
    fun registerAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.register(subscriber)
        }
    }

    /**
     * Removes all [EventHandler] annotated [Listener] objects from the registry.
     *
     * @param subscriber Event subscriber instance.
     */
    fun unregister(subscriber: Any)

    /**
     * Removes all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want removed from the registry.
     */
    fun unregisterAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.unregister(subscriber)
        }
    }

    /**
     * Post an event to be processed by the subscribed methods or listener objects.
     *
     * @param T Event type.
     * @param event Instance of [T] to post.
     */
    fun <T : Any> dispatch(event: T)
}