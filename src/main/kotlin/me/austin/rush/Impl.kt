package me.austin.rush

import kotlinx.coroutines.runBlocking
import net.jodah.typetools.TypeResolver
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Basic implementation of [EventBus]
 */
open class EventManager : EventBus {
    override val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener<*>>>()

    private val cache = ConcurrentHashMap<Any, MutableList<Listener<*>>>()

    override fun register(listener: Listener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
            if (it.contains(listener)) return

            var index = 0

            while (index < it.size) {
                if (it[index].priority < listener.priority) break

                index++
            }

            it.add(index, listener)
        }
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber, subscriber::listeners)) this.register(listener)
    }

    override fun unregister(subscriber: Any) {
        for (listener in subscriber.listeners) this.unregister(listener)
    }

    override fun <T : Any> dispatch(event: T) {
        (registry[event::class] as? MutableList<Listener<T>>)?.let { synchronized(it) { it.forEach { it(event) } } }
    }

    fun <T : Cancellable> dispatch(event: T): T {
        (registry[event::class] as? MutableList<Listener<T>>)?.let {
            synchronized(it) {
                for (listener in it) {
                    listener(event)
                    if (event.isCancelled) break
                }
            }
        }

        return event
    }
}

// Most of this is pasted from bush https://github.com/therealbush/eventbus-kotlin, check him out if you want to see actually good code

private val KCallable<*>.isListener
    get() = this.findAnnotation<EventHandler>() != null && this.returnType.isSubtypeOf(typeOf<Listener<*>>())

private val <T : Any> KClass<T>.listeners
    get() = this.declaredMembers.filter(KCallable<*>::isListener) as List<KCallable<Listener<*>>>

private val Any.listeners
    get() = this::class.listeners.mapTo(mutableListOf()) { it.handleCall(this) }

private fun <T : Any> KCallable<T>.handleCall(receiver: Any? = null): T {
    val accessible = this.isAccessible
    this.isAccessible = true

    // This will get a both static and non-static listeners in the jvm
    return try { call(receiver) } catch (e: Throwable) { call() } finally { this.isAccessible = accessible }
}

/**
 * This is for making simple, non-verbose listeners
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 */
inline fun <reified T : Any> listener(noinline action: (T) -> Unit) = LambdaListener(T::class, -50, action)

/**
 * This is for making listeners in Kotlin specifically, as it has less overhead
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> listener(
    noinline action: (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class,
) = LambdaListener(target, priority, action)

/** Implementation of [Listener] that uses a lambda function as its target */
open class LambdaListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: (T) -> Unit
) : Listener<T> {
    /**
     * This is for creating listeners in Java specifically, as it uses consumers which don't have a return statement
     *
     * @param T type the consumer accepts
     * @param action consumer the listeners will call when an event is posted
     * @param priority the priority which this listener will be called when an event is posted
     */
    @JvmOverloads
    constructor(action: Consumer<T>, priority: Int = -50) : this(action.paramType.kotlin, priority, action::accept)

    override operator fun invoke(param: T) = this.action(param)
}

/**
 * Use this to make listeners that can use async/await functions
 *
 * @param T the type the consumer accepts
 * @param action the consumer that will be called on an event posting
 */
inline fun <reified T : Any> asyncListener(noinline action: suspend (T) -> Unit) = AsyncListener(T::class, -50, action)

/**
 * This is for making listeners that can use async/await functions
 *
 * @param T type the lambda will accept
 * @param action consumer the listeners will call when an event is posted
 * @param priority the priority which this listener will be called when an event is posted
 * @param target class that the listener will listen for
 */
inline fun <reified T : Any> asyncListener(
    noinline action: suspend (T) -> Unit, priority: Int = -50, target: KClass<T> = T::class
) = AsyncListener(target, priority, action)

open class AsyncListener<T : Any> @PublishedApi internal constructor(
    override val target: KClass<T>, override val priority: Int, private val action: suspend (T) -> Unit
) : Listener<T> {
    /**
     * This is for creating listeners in Java specifically, as it uses consumers which don't have a return statement
     *
     * @param T type the consumer accepts
     * @param action consumer the listeners will call when an event is posted
     * @param priority the priority which this listener will be called when an event is posted
     */
    @JvmOverloads
    constructor(action: Consumer<T>, priority: Int = -50) : this(action.paramType.kotlin, priority, action::accept)

    override operator fun invoke(param: T) = runBlocking { action(param) }
}

private val <T : Any> Consumer<T>.paramType
    get() = TypeResolver.resolveRawArgument(Consumer::class.java, this.javaClass) as Class<T>

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry
 */
annotation class EventHandler