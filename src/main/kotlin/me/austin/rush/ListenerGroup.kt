package me.austin.rush

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList

class ListenerGroup<T : Any> : EventDispatcher {
    private val mainList = CopyOnWriteArrayList<Listener<T>>()
    private val asyncList = CopyOnWriteArrayList<Listener<T>>()

    fun register(listener: Listener<T>) {

    }

    fun unregister(listener: Listener<*>) {

    }

    override fun <T : Any> dispatch(event: T) {
        runBlocking {
            launch {
                for (listener in asyncList) listener.invoke(event)
            }
        }

        for (listener in mainList) listener.invoke(event)
    }
}