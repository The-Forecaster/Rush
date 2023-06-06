package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * Implementation of [Listener] that uses a lambda function as its target.
 *
 * @constructor Creates a [Listener] with the specified parameters.
 *
 * @param target The [KClass] which [action] will accept.
 * @param priority How highly this listener should be prioritized when an event is posted.
 * @param action The lambda which will be called when an event is posted.
 */
open class LambdaListener @PublishedApi internal constructor(
    override val target: KClass<*>, override val priority: Byte, action: (Nothing) -> Unit
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
 * @param action The lambda the listeners will call when an event is posted.
 *
 * @return A new [LambdaListener] with the specified parameters.
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit): LambdaListener {
    return LambdaListener(T::class, -8, action)
}

/**
 * This is for making listeners in Kotlin.
 *
 * @param T The type that [action] will accept.
 * @param action The lambda the listeners will call when an event is posted.
 * @param priority How highly this listener should be prioritized when an event is posted. Will default to -8
 * @param target The [KClass] that this listener will listen for. Will default to [T]
 *
 * @return A new [LambdaListener] with the specified parameters.
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Byte = -8, target: KClass<*> = T::class
): LambdaListener {
    return LambdaListener(target, priority, action)
}

/**
 * Constructor for java.
 *
 * @param action The [Consumer] which will be called when an event is posted.
 * @param target The [Class] which [action] will accept.
 * @param priority How highly this listener should be prioritized will default to -8.
 *
 * @return A new [LambdaListener] with the [Consumer] as its action.
 */
@JvmOverloads
fun <T : Any> listener(
    action: Consumer<T>, target: Class<T>, priority: Byte = -8
): LambdaListener {
    return LambdaListener(target.kotlin, priority, action::accept)
}

/**
 * Implementation of [Listener] that uses an asynchronous lambda function as its target.
 *
 * @constructor Creates a listener with the specified parameters.
 *
 * @param target The [KClass] which the [action] will accept.
 * @param priority How highly this [Listener] should be prioritized.
 * @param scope The [CoroutineScope] to call [action] in. Will default to [defaultScope].
 * @param action The lambda which will be called when an event is posted.
 */
open class AsyncListener @PublishedApi internal constructor(
    override val target: KClass<*>,
    override val priority: Byte,
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
 * @param T The type that [action] will accept.
 * @param action The Lambda that will call when an event is posted.
 *
 * @return A new [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit): AsyncListener {
    return AsyncListener(T::class, -8, defaultScope, action)
}

/**
 * This is for making listeners that can use async/await functions.
 *
 * @param T The type that [action] will accept.
 * @param action The lambda that will call when an event is posted.
 * @param priority The priority which this listener will be called when an event is posted. Will default to -8.
 * @param target The [KClass] that the listener will listen for. Will default to [T].
 * @param scope [CoroutineScope] to call the lambda in. Will default to [defaultScope].
 *
 * @return A new [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(
    noinline action: suspend (T) -> Unit,
    priority: Byte = -8,
    target: KClass<T> = T::class,
    scope: CoroutineScope = defaultScope
): AsyncListener {
    return AsyncListener(target, priority, scope, action)
}