package trans.rights.event.listener.impl

import me.austin.rush.annotation.DEFAULT
import trans.rights.event.listener.Listener
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
inline fun <reified T : Any> listener(
    action: Consumer<T>,
    priority: Int = DEFAULT,
    target: Class<T> = T::class.java,
): Listener<T> = LambdaListener(action::accept, priority, target.kotlin)

/**
 * This is for making simple, non-verbose listeners
 *
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit): LambdaListener<*> =
    listener(action, DEFAULT, T::class)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit,
    priority: Int = DEFAULT,
    target: KClass<T> = T::class,
): LambdaListener<T> = LambdaListener(action, priority, target)

/** Implementation of Listener that uses a lambda function as its target */
open class LambdaListener<T : Any>(
    private val action: (T) -> Unit,
    override val priority: Int,
    override val target: KClass<T>,
) : Listener<T> {
    override operator fun invoke(param: T) = this.action(param)
}
