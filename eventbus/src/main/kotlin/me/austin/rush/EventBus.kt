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
     * Adds all [EventHandler] annotated [Listener] objects into the registry.
     *
     * @param subscriber Object you want to be searched for listeners to be added to the registry.
     */
    fun subscribe(subscriber: Any)

    /**
     * Adds all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want to be added to the registry.
     */
    fun subscribeAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.subscribe(subscriber)
        }
    }

    /**
     * Removes all [EventHandler] annotated [Listener] objects from the registry.
     *
     * @param subscriber Event subscriber instance.
     */
    fun unsubscribe(subscriber: Any)

    /**
     * Removes all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want removed from the registry.
     */
    fun unsubscribeAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.unsubscribe(subscriber)
        }
    }

    /**
     * Post an event to be processed by the subscribed methods or listener objects.
     *
     * @param T Event type.
     * @param event Instance of [T] to post.
     */
    fun <T : Any> post(event: T)
}