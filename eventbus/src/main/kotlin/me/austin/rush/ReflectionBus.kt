package me.austin.rush

/**
 * Basic structure for an event dispatcher.
 *
 * @author Austin
 * @since 2022
 */
interface ReflectionBus : EventBus {
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

    fun <T : Any> postRecursive(event: T)
}