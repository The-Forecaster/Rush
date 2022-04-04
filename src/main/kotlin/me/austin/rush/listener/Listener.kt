package me.austin.rush.listener

import kotlin.math.max

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 */
interface Listener<T : Any> : Comparable<Listener<T>> {

    /** the priority that the listener will be called upon(use wisely) */
    val priority: Int

    /** the object that this listener was defined in */
    val parent: Any

    /** the class of the target event */
    val target: Class<T>

    /**
     * Processes an event passed through this listener
     *
     * @param param event object that is being processed
     */
    operator fun invoke(param: T)

    override operator fun compareTo(other: Listener<T>): Int {
        return max(priority, other.priority)
    }
}
