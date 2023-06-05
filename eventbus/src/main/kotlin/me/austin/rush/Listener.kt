package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 * @since 2022
 */
interface Listener : Comparable<Listener> {
    /** The class of the target event */
    val target: KClass<*>

    /** The priority that the listener will be called upon */
    val priority: Int

    /**
     * Processes an event passed through this listener.
     *
     * @param param Event object that is being processed.
     */
    operator fun invoke(param: Any)

    override fun compareTo(other: Listener): Int {
        // Listeners with higher priorities should come first, so negate the compare result
        return -this.priority.compareTo(other.priority)
    }
}