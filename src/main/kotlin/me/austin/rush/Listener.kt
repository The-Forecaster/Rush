package me.austin.rush

import kotlinx.coroutines.runBlocking
import net.jodah.typetools.TypeResolver
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * This is for making simple, non-verbose listeners
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit) = LambdaListener(T::class, -50, action)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class,
) = LambdaListener(target, priority, action)

/** Implementation of [Listener] that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: (T) -> Unit
) : Listener<T> {
    /**
     * This is for creating listeners in Java specifically, as it uses consumers which don't have a return statement
     *
     * @param T type the consumer accepts
     * @param action consumer the listeners will call when an event is posted
     * @param priority the priority which this listener will be called when an event is posted
     * @param target class that the listener will listen for
     */
    @JvmOverloads
    constructor(action: Consumer<T>, priority: Int = -50, target: Class<T> = action.paramType) : this(
        target.kotlin, priority, action::accept
    )

    override operator fun invoke(param: T) = this.action(param)
}

/**
 * Use this to make listeners that can use async/await functions
 *
 * @param T the type the consumer accepts
 * @param action the consumer that will be called on an event posting
 */
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit) = AsyncListener(T::class, -50, action)

/**
 * This is for making listeners that can use async/await functions
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> asyncListener(
    noinline action: suspend (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class
) = AsyncListener(target, priority, action)

open class AsyncListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: suspend (T) -> Unit
) : Listener<T> {
    override operator fun invoke(param: T) = runBlocking { action(param) }
}

private val <T : Any> Consumer<T>.paramType
    get() = TypeResolver.resolveRawArgument(Consumer::class.java, this.javaClass) as Class<T>

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry
 */
annotation class EventHandler

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
