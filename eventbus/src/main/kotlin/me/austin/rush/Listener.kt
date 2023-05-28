package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 * @since 2022
 */
interface Listener {
    /** the class of the target event */
    val target: KClass<*>

    /** the priority that the listener will be called upon(use wisely) */
    val priority: Int

    /**
     * Processes an event passed through this listener
     *
     * @param param event object that is being processed
     */
    operator fun invoke(param: Any)
}

/**
 * Implementation of [Listener] that uses a lambda function as its target
 *
 * @constructor Creates a listener with the specified parameters
 *
 * @param target class which the listener will accept
 * @param priority how highly this listener should be prioritized
 * @param lambda action which will be called when an event is posted
 */
class LambdaListener @PublishedApi internal constructor(
    override val target: KClass<*>, override val priority: Int, lambda: (Nothing) -> Unit
) : Listener {
    /**
     * Real action that will be called when an event is posted
     */
    internal val action = lambda as (Any) -> Unit

    /**
     * Creates a listener, constructor for java
     *
     * @param action consumer which will be called when an event is posted
     * @param priority how highly this listener should be prioritized
     * @param target class which the listener will accept
     */
    @JvmOverloads
    constructor(action: Consumer<*>, priority: Int = -50, target: Class<*>) : this(
        target.kotlin, priority, action::accept
    )

    override operator fun invoke(param: Any) {
        this.action(param)
    }
}

/**
 * This is for making listeners with less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 *
 * @return [LambdaListener] with the action
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit): LambdaListener {
    return LambdaListener(T::class, -50, action)
}

/**
 * This is for making listeners in Kotlin
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 *
 * @return [LambdaListener] with the action
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class
): LambdaListener {
    return LambdaListener(target, priority, action)
}

/**
 * Implementation of [Listener] that uses an asynchronous lambda function as its target
 *
 * @constructor Creates a listener with the specified parameters
 *
 * @param target class which the listener will accept
 * @param priority how highly this listener should be prioritized
 * @param lambda action which will be called when an event is posted
 */
class AsyncListener @PublishedApi internal constructor(
    override val target: KClass<*>,
    override val priority: Int,
    private val scope: CoroutineScope,
    lambda: suspend (Nothing) -> Unit
) : Listener {
    /**
     * Real action that will be called when an event is posted
     */
    internal val action = lambda as (Any) -> Unit

    override operator fun invoke(param: Any) {
        scope.launch { action(param) }
    }
}

/**
 * This is for making asynchronous listeners with less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 *
 * @return [AsyncListener] with the action
 */
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit): AsyncListener {
    return AsyncListener(T::class, -50, defaultScope, action)
}

/**
 * This is for making listeners that can use async/await functions
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 *
 * @return [AsyncListener] with the action
 */
inline fun <reified T : Any> asyncListener(
    noinline action: suspend (T) -> Unit,
    priority: Int = -50,
    target: KClass<T> = T::class,
    scope: CoroutineScope = defaultScope
): AsyncListener {
    return AsyncListener(target, priority, scope, action)
}