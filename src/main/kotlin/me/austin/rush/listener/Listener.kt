package me.austin.rush.listener

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 */
interface Listener<T> : Comparable<Listener<T>> {

    /** the priority that the listener will be called upon(use wisely) */
    val priority: Int

    /** the class of the target event */
    val target: Class<T>

    /**
     * Processes an event passed through this listener
     *
     * @param param event object that is being processed
     */
    operator fun invoke(param: T)

    override operator fun compareTo(other: Listener<T>): Int {
        return if (priority > other.priority) 1 else if (priority == other.priority) 0 else -1
    }
}
