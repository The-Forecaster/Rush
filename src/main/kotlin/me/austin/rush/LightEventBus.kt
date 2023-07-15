package me.austin.rush

import java.util.*
import kotlin.reflect.KClass

class LightEventBus : EventBus {
    private val subscribers = mutableMapOf<KClass<*>, MutableList<Listener>>()

    override fun subscribe(listener: Listener) {
        val list = this.subscribers[listener.target]

        if (list == null) {
            this.subscribers[listener.target] = mutableListOf(listener)
        } else {
            if (listener in list) {
                return
            }

            list.add(Collections.binarySearch(list, listener).let { i ->
                if (i < 0) {
                    -i - 1
                } else {
                    i
                }
            }, listener)
        }
    }

    override fun unsubscribe(listener: Listener) {
        this.subscribers[listener.target]?.let { list ->
            if (list.remove(listener) && list.isEmpty()) {
                this.subscribers.remove(listener.target)
            }
        }
    }

    override fun <T : Any> post(event: T) {
        this.subscribers[event::class]?.let { list ->
            for (listener in list) {
                listener(event)
            }
        }
    }
}