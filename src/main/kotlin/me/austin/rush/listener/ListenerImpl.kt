package me.austin.rush.listener

import kotlinx.coroutines.runBlocking
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

/**
 * This is for creating listeners in Java specifically, as it uses consumers which don't have a return statement
 *
 * @param T type the consumer accepts
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
@JvmOverloads
fun <T : Any> listener(
    action: Consumer<T>, priority: Int = -50,

    // This is a hack I found online, uses lots of bullshit so if someone sees a better way to do this I'm all ears
    target: Class<T> = (action::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, action::class.typeParameters[0].starProjectedType))).classifier as KClass<T>).java
) = LambdaListener(target.kotlin, priority, action::accept)

/**
 * This is for making simple, non-verbose listeners
 *
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit) = LambdaListener(T::class, -50, action)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    target: KClass<T> = T::class, priority: Int = -50, noinline action: (T) -> Unit
) = LambdaListener(target, priority, action)

/** Implementation of [Listener] that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: (T) -> Unit
) : Listener<T> {
    override operator fun invoke(param: T) = this.action(param)
}

inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit) = AsyncListener(T::class, -50, action)

inline fun <reified T : Any> asyncListener(
    target: KClass<T> = T::class, priority: Int = -50, noinline action: suspend (T) -> Unit
) = AsyncListener(target, priority, action)

open class AsyncListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: suspend (T) -> Unit
) : Listener<T> {
    override operator fun invoke(param: T) = runBlocking {
        action(param)
    }
}

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry
 */
annotation class EventHandler
