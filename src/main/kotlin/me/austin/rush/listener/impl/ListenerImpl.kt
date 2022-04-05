package me.austin.rush.listener.impl

import me.austin.rush.annotation.DEFAULT
import me.austin.rush.listener.Listener

inline fun <reified T : Any> Any.lambdaListener(noinline action: (T) -> Unit): LambdaListener<T> {
    return lambdaListener(action, DEFAULT)
}

inline fun <reified T : Any> Any.lambdaListener(
    noinline action: (T) -> Unit, priority: Int
): LambdaListener<T> {
    return listener(action, priority, this, T::class.java)
}

fun <T : Any> listener(
    action: (T) -> Unit, priority: Int, parent: Any, target: Class<T>
): LambdaListener<T> {
    return LambdaListener(action, priority, parent, target)
}

/** Implementation of Listener that uses a lambda function as its target */
open class LambdaListener<T>(
    private val action: (T) -> Unit, override val priority: Int, override val parent: Any, override val target: Class<T>
) : Listener<T> {
    override operator fun invoke(param: T) {
        return this.action(param)
    }
}