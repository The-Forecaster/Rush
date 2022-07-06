package me.austin.rush.listener

import kotlin.reflect.KClass

/**
 * This is for making simple, non-verbose listeners
 *
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit) = listener(T::class, DEFAULT, action)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    target: KClass<T> = T::class, priority: Int = DEFAULT, noinline action: (T) -> Unit
) = LambdaListener(target, priority, action)

/** Implementation of Listener that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, internal val action: (T) -> Unit
) : Listener<T> {
    override operator fun invoke(param: T) = this.action(param)
}

const val DEFAULT = -50
const val LOWEST = -200
const val LOW = -100
const val MEDIUM = 0
const val HIGH = 100
const val HIGHEST = 200

/**
 * Annotate a listener with this class to mark it for adding to the eventbus' registry
 */
annotation class EventHandler
