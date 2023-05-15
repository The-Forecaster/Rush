package me.austin.rush

import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 * @since 2022
 */
interface IListener<T : Any> {
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

/** Implementation of [IListener] that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, protected val action: (T) -> Unit
) : IListener<T> {
    @JvmOverloads
    constructor(action: Consumer<T>, priority: Int = -50, target: Class<T>) : this(
        target.kotlin, priority, action::accept
    )

    override operator fun invoke(param: T) {
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
inline fun <reified T : Any> listener(noinline action: (T) -> Unit): LambdaListener<T> {
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
    noinline action: (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class,
): LambdaListener<T> {
    return LambdaListener(target, priority, action)
}

/** Implementation of [IListener] that uses an async/await function as its action */
open class AsyncListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, protected val action: suspend (T) -> Unit
) : IListener<T> {
    override operator fun invoke(param: T) {
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
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit): AsyncListener<T> {
    return AsyncListener(T::class, -50, action)
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
    noinline action: suspend (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class
): AsyncListener<T> {
    return AsyncListener(target, priority, action)
}