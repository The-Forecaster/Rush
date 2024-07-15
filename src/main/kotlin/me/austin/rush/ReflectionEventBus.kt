package me.austin.rush

/**
 * Event Bus that uses reflection.
 *
 * @author Austin
 * @since 2022
 */
interface ReflectionEventBus : EventBus {
    /**
     * Adds all [EventHandler] annotated [Listener] objects into the registry.
     *
     * @param subscriber Object you want to be scanned.
     */
    fun subscribe(subscriber: Any)

    /**
     * Adds all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want to be scanned.
     */
    fun subscribeAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.subscribe(subscriber)
        }
    }

    /**
     * Removes all [EventHandler] annotated [Listener] objects from the registry.
     *
     * @param subscriber Object you want removed from the registry.
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
     * Posts an event and all it's superclasses to all registered listeners
     */
    fun <T : Any> postRecursive(event: T)
}