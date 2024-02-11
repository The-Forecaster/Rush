package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    @Suppress("UNCHECKED_CAST") // So we can avoid using generics
    private val action = action as (Any) -> Unit

    override operator fun invoke(param: Any) {
        this.action(param)
    }
}

/**
 * This is for making listeners in Kotlin.
 *
 * @param T The type that [action] will accept.
 * @param priority How highly this listener should be prioritized when an event is posted. Will default to `-50`.
 * @param action The lambda the listeners will call when an event is posted.
 *
 * @return A new [LambdaListener] with the specified parameters.
 */
inline fun <reified T : Any> listener(
    priority: Int = -50, noinline action: (T) -> Unit
): Listener {
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
): Listener {
    return LambdaListener(target.kotlin, priority, action::accept)
}

/**
 * Implementation of [Listener] that uses an asynchronous lambda function as its target.
 *
 * @constructor Creates an [AsyncListener] with the specified parameters.
 *
 * @param target The [KClass] which the [action] will accept.
 * @param priority How highly this [Listener] should be prioritized.
 * @param scope The [CoroutineScope] to call [action] in.
 * @param action The lambda which will be called when an event is posted.
 */
class AsyncListener @PublishedApi internal constructor(
    override val target: KClass<*>,
    override val priority: Int,
    private val scope: CoroutineScope,
    action: suspend (Nothing) -> Unit
) : Listener {
    @Suppress("UNCHECKED_CAST") // So we can avoid using generics
    private val action = action as suspend (Any) -> Unit

    override operator fun invoke(param: Any) {
        this.scope.launch {
            this@AsyncListener.action(param)
        }
    }
}

/**
 * This is for making listeners that can use async/await functions.
 *
 * @param T The type that [action] will accept.
 * @param priority The priority which this listener will be called when an event is posted. Will default to `-50`.
 * @param scope The [CoroutineScope] to call [action] in. Will default to [Dispatchers.Default]
 * @param action The lambda that will call when an event is posted.
 *
 * @return A new [AsyncListener] with the action.
 */
inline fun <reified T : Any> asyncListener(
    priority: Int = -50,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    noinline action: suspend (T) -> Unit
): Listener {
    return AsyncListener(T::class, priority, scope, action)
}