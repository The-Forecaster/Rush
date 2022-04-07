package me.austin.rush.bus.impl

import me.austin.rush.annotation.EventHandler
import me.austin.rush.listener.Listener
import me.austin.rush.listener.impl.LambdaListener
import java.lang.reflect.Field
import java.util.*
import java.util.Arrays.*
import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Collectors

open class EventManager(type: Class<out Listener<*>> = LambdaListener::class.java) : AbstractEventBus(type) {
    override fun registerFields(subscriber: Any) {
        val lists: List<Listener<*>> = when (subscriber) {
            is Listener<*> -> listOf(subscriber)
            is Collection<*> -> subscriber.stream().filter { this.type.isAssignableFrom(it!!::class.java) }.collect(Collectors.toList()) as List<Listener<*>>
            else -> subscriber.javaClass.declaredFields.filter(this::isValid) as List<Listener<*>>
        }

        if (lists.isEmpty()) return

        lists.stream().forEach { listener ->
            this.registry.getOrPut(listener.target, ::CopyOnWriteArraySet).run {
                this.add(listener)
                this.toSortedSet(Comparator.comparingInt(Listener<*>::priority))
            }
        }
    }

    override fun unregisterFields(subscriber: Any) {
        stream(subscriber.javaClass.declaredFields).filter(this::isValid).forEach { field ->
            this.registry[field.type]?.remove(field.get(subscriber))
        }
    }

    private fun isValid(field: Field): Boolean {
        return field.isAnnotationPresent(EventHandler::class.java) && this.type.isAssignableFrom(field.type)
    }
}
