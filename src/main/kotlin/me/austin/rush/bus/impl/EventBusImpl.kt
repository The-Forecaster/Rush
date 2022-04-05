package me.austin.rush.bus.impl

import me.austin.rush.annotation.EventHandler
import me.austin.rush.listener.Listener
import me.austin.rush.listener.impl.LambdaListener
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

open class EventManager(type: Class<*>) : AbstractEventBus(type) {
    constructor() : this(LambdaListener::class.java)

    override fun registerFields(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredFields).filter(this::isValid).forEach { field ->
            this.registry.getOrPut(field.asListener(subscriber).target, ::CopyOnWriteArraySet).run {
                this.add(field.asListener(subscriber))
                this.toSortedSet(Comparator.comparingInt(Listener<*>::priority))
            }
        }
    }

    override fun unregisterFields(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredFields).filter(this::isValid).forEach { field ->
            this.registry[field.type]?.remove(field.get(subscriber))
        }
    }

    private fun isValid(field: Field): Boolean = field.isAnnotationPresent(EventHandler::class.java) && this.type.isAssignableFrom(field.type)
}

private fun Field.asListener(parent: Any): LambdaListener<*> {
    this.trySetAccessible()

    return this.get(parent) as LambdaListener<*>
}
