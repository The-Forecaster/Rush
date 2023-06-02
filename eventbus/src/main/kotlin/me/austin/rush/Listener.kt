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

/**
 * Implementation of [Listener] that uses a lambda function as its target.
 *
 * @constructor Creates a listener with the specified parameters.
 *
 * @param target [KClass] which the listener will accept.
 * @param priority How highly this listener should be prioritized.
 * @param action Lambda which will be called when an event is posted.
 */
open class LambdaListener @PublishedApi internal constructor(
    override val target: KClass<*>, override val priority: Int, action: (Nothing) -> Unit
) : Listener {
    /**
     * Real action that will be called when an event is posted.
     */
    // This cast should never fail
    internal val action = action as (Any) -> Unit

    override operator fun invoke(param: Any) {
        this.action(param)
    }
}

/**
 * This is for making listeners with less overhead.
 *
 * @param T The Type that [action] will accept.
 * @param action Lambda the listeners will call when an event is posted.
 *
 * @return [LambdaListener] with the action.
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit): LambdaListener {
    return LambdaListener(T::class, -50, action)
}

/**
 * This is for making listeners in Kotlin.
 *
 * @param T Type the lambda will accept.
 * @param action Lambda the listeners will call when an event is posted.
 * @param priority The priority which this listener will be called when an event is posted.
 * @param target [KClass] that the listener will listen for.
 *
 * @return [LambdaListener] with the action.
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class
): LambdaListener {
    return LambdaListener(target, priority, action)
}

/**
 * Constructor for java.
 *
 * @param action [Consumer] which will be called when an event is posted.
 * @param target [Class] which the listener will accept.
 * @param priority How highly this listener should be prioritized.
 */
@JvmOverloads
fun <T : Any> listener(
    action: Consumer<T>,
    target: Class<T>,
    priority: Int = -50
): LambdaListener {
    return LambdaListener(target.kotlin, priority, action::accept)
}

/**
 * Implementation of [Listener] that uses an asynchronous lambda function as its target.
 *
 * @constructor Creates a listener with the specified parameters.
 *
 * @param target [KClass] which the listener will accept.
 * @param priority How highly this [Listener] should be prioritized.
 * @param scope [CoroutineScope] to call the lambda in. Will default to [defaultScope].
 * @param action Lambda which will be called when an event is posted.
 */
open class AsyncListener @PublishedApi internal constructor(
    override val target: KClass<*>,
    override val priority: Int,
    private val scope: CoroutineScope,
    action: suspend (Nothing) -> Unit
) : Listener {
    /**
     * Real action that will be called when an event is posted.
     */
    // This cast should never ever fail
    internal val action = action as suspend (Any) -> Unit

    override operator fun invoke(param: Any) {
        scope.launch { action(param) }
    }
}

/**
 * This is for making asynchronous listeners with less overhead.
 *
 * @param T Type the lambda will accept.
 * @param action Lambda the listeners will call when an event is posted.
 *
 * @return [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit): AsyncListener {
    return AsyncListener(T::class, -50, defaultScope, action)
}

/**
 * This is for making listeners that can use async/await functions.
 *
 * @param T The type that the lambda will accept.
 * @param action Lambda the listeners will call when an event is posted.
 * @param priority The priority which this listener will be called when an event is posted.
 * @param target [KClass] that the listener will listen for. Will default to [T].
 * @param scope [CoroutineScope] to call the lambda in. Will default to [defaultScope].
 *
 * @return [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(
    noinline action: suspend (T) -> Unit,
    priority: Int = -50,
    target: KClass<T> = T::class,
    scope: CoroutineScope = defaultScope
): AsyncListener {
    return AsyncListener(target, priority, scope, action)
}