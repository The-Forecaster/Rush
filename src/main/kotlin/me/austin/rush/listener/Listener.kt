package me.austin.rush.listener

import kotlin.reflect.KClass

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 */
interface Listener<T: Any> : Comparable<Listener<*>> {

    /** the class of the target event */
    val target: KClass<T>

    /** the priority that the listener will be called upon(use wisely) */
    val priority: Int

    /**
     * Processes an event passed through this listener
     *
     * @param param event object that is being processed
     */
    operator fun invoke(param: T)

    override operator fun compareTo(other: Listener<*>) = if (this.priority > other.priority) 1 else if (this.priority == other.priority) 0 else -1
}
