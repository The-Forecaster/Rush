package me.austin.rush.listener

import net.jodah.typetools.TypeResolver
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * This is for creating listeners in Java specifically, as it uses consumers which don't have a return statement
 *
 * @param T type the consumer accepts
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
@JvmOverloads
fun <T : Any> listener(
    action: Consumer<T>,
    priority: Int = DEFAULT,
    target: Class<T> = TypeResolver.resolveRawArgument(
        Consumer::class.java,
        action.javaClass
    ) as Class<T>
) = LambdaListener(action::accept, priority, target.kotlin)

/**
 * This is for making simple, non-verbose listeners
 *
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit) = listener(action, DEFAULT, T::class)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Int = DEFAULT, target: KClass<T> = T::class
) = LambdaListener(action, priority, target)

/** Implementation of Listener that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    internal val action: (T) -> Unit, override val priority: Int, override val target: KClass<T>
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
