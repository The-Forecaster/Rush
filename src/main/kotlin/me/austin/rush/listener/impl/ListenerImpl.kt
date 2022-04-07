package me.austin.rush.listener.impl

import me.austin.rush.annotation.DEFAULT
import me.austin.rush.listener.Listener

inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit,
    priority: Int = DEFAULT,
    target: Class<T> = T::class.java
): LambdaListener<T> {
    return LambdaListener(action, priority, target)
}

/** Implementation of Listener that uses a lambda function as its target */
open class LambdaListener<T>(
    private val action: (T) -> Unit,
    override val priority: Int,
    override val target: Class<T>
) : Listener<T> {
    override operator fun invoke(param: T) {
        return this.action(param)
    }
}
