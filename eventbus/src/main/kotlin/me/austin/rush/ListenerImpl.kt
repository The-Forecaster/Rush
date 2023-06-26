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
class LambdaListener @PublishedApi internal constructor(
    override val target: KClass<*>, override val priority: Int, action: (Nothing) -> Unit
) : Listener {
    // So we can avoid using generics
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
    return LambdaListener(T::class, -50, action)
}

/**
 * This is for making listeners in Kotlin.
 *
 * @param T The type that [action] will accept.
 * @param target The [KClass] that this listener will listen for. Will default to [T].
 * @param priority How highly this listener should be prioritized when an event is posted. Will default to `-50`.
 * @param action The lambda the listeners will call when an event is posted.
 *
 * @return A new [LambdaListener] with the specified parameters.
 */
inline fun <reified T : Any> listener(
    priority: Int = -50, noinline action: (T) -> Unit
): LambdaListener {
    return LambdaListener(T::class, priority, action)
}

/**
 * Constructor for java.
 *
 * @param action The [Consumer] which will be called when an event is posted.
 * @param target The [Class] which [action] will accept.
 * @param priority How highly this listener should be prioritized will default to `-50`.
 *
 * @return A new [LambdaListener] with the [Consumer] as its action.
 */
@JvmOverloads
fun <T : Any> listener(
    target: Class<T>, priority: Int = -50, action: Consumer<T>
): LambdaListener {
    return LambdaListener(target.kotlin, priority, action::accept)
}

/**
 * Implementation of [Listener] that uses an asynchronous lambda function as its target.
 *
 * @constructor Creates an [AsyncListener] with the specified parameters.
 *
 * @param target The [KClass] which the [action] will accept.
 * @param priority How highly this [Listener] should be prioritized.
 * @param scope The [CoroutineScope] to call [action] in. Will default to [defaultScope].
 * @param action The lambda which will be called when an event is posted.
 */
class AsyncListener @PublishedApi internal constructor(
    override val target: KClass<*>,
    override val priority: Int,
    private val scope: CoroutineScope,
    action: suspend (Nothing) -> Unit
) : Listener {
    // So we can avoid using generics
    internal val action = action as suspend (Any) -> Unit

    override operator fun invoke(param: Any) {
        scope.launch {
            action(param)
        }
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
    return AsyncListener(T::class, -50, defaultScope, action)
}

/**
 * This is for making listeners that can use async/await functions.
 *
 * @param T The type that [action] will accept.
 * @param target The [KClass] that the listener will listen for. Will default to [T].
 * @param priority The priority which this listener will be called when an event is posted. Will default to `-50`.
 * @param scope [CoroutineScope] to call the lambda in. Will default to [defaultScope].
 * @param action The lambda that will call when an event is posted.
 *
 * @return A new [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(
    priority: Int = -50,
    scope: CoroutineScope = defaultScope,
    noinline action: suspend (T) -> Unit
): AsyncListener {
    return AsyncListener(T::class, priority, scope, action)
}