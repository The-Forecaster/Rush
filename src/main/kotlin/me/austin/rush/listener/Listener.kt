package me.austin.rush.listener

import kotlin.reflect.KClass

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 */
interface Listener<T : Any> {

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
}
