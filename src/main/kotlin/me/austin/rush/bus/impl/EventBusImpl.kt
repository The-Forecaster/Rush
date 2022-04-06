package me.austin.rush.bus.impl

import me.austin.rush.annotation.EventHandler
import me.austin.rush.listener.Listener
import me.austin.rush.listener.impl.LambdaListener
import java.lang.reflect.Field
import java.util.*
import java.util.Arrays.*
import java.util.concurrent.CopyOnWriteArraySet

open class EventManager(type: Class<*>) : AbstractEventBus(type) {
    constructor() : this(LambdaListener::class.java)

    override fun registerFields(subscriber: Any) {
        val lists: Collection<Listener<*>> = when (subscriber) {
            is Listener<*> -> listOf(subscriber)
            is Collection<*> -> subscriber as Collection<Listener<*>>
            else -> subscriber.javaClass.declaredFields.filter(this::isValid) as Collection<Listener<*>>
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

private fun Field.asListener(parent: Any): LambdaListener<*> {
    this.trySetAccessible()

    return this.get(parent) as LambdaListener<*>
}
