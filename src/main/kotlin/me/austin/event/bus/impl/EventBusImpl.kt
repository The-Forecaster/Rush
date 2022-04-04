package me.austin.event.bus.impl

import me.austin.event.annotation.EventHandler
import me.austin.event.bus.AbstractEventBus
import me.austin.event.bus.ListenerType
import me.austin.event.listener.Listener
import me.austin.event.listener.impl.LambdaListener
import me.austin.event.listener.impl.MethodListener
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

open class EventManager(type: ListenerType) : AbstractEventBus(type) {
    override fun registerFields(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredFields).filter(this::isValid).forEach { field ->
            this.registry.getOrPut(field.asListener(subscriber).target, ::CopyOnWriteArraySet).run {
                this.add(field.asListener(subscriber))
                this.toSortedSet(Comparator.comparingInt(Listener<*>::priority))
            }
        }
    }

    override fun registerMethods(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredMethods).filter(Method::isValid).forEach { method ->
            this.registry.getOrPut(method.parameters[0].type, ::CopyOnWriteArraySet).run {
                this.add(method.asListener(subscriber))
                this.toSortedSet(Comparator.comparingInt(Listener<*>::priority))
            }
        }
    }

    override fun unregisterFields(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredFields).filter(this::isValid).forEach { field ->
            this.registry[field.type]?.remove(field.get(subscriber))
        }
    }

    override fun unregisterMethods(subscriber: Any) {
        Arrays.stream(subscriber.javaClass.declaredMethods).filter(Method::isValid).forEach { method ->
            this.registry[method.parameters[0].type]?.remove(method.asListener(subscriber))
        }
    }

    private fun isValid(field: Field): Boolean = field.isAnnotationPresent(EventHandler::class.java) && Listener::class.java.isAssignableFrom(field.type)
}

private fun Method.isValid(): Boolean = this.isAnnotationPresent(EventHandler::class.java) && this.parameterCount == 1

private fun Method.asListener(parent: Any): MethodListener<*> {
    this.trySetAccessible()

    return MethodListener(
        this,
        this.getAnnotation(EventHandler::class.java).priority,
        parent,
        this.parameters[0]::class.java
    )
}

private fun Field.asListener(parent: Any): LambdaListener<*> {
    this.trySetAccessible()

    return this.get(parent) as LambdaListener<*>
}
