package me.austin.rush

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Annotate a [Listener] with this class to mark it for adding to a [ReflectionEventBus].
 *
 * @author Austin
 * @since 2022
 */
annotation class EventHandler

// Check out https://github.com/therealbush/eventbus-kotlin if you want to see where this logic comes from

/**
 * Using [KClass.members] only returns public members, and using [KClass.declaredMembers]
 * doesn't return inherited members. This function, while slower than those two, is able
 * to retrieve all [KCallable] members inside a class.
 *
 * @return A [Sequence] of all members, private and inherited.
 */
private val KClass<*>.allMembers: Sequence<KCallable<*>>
    get() {
        // Returns all fields, public or otherwise, in this class and all superclasses of it
        // asSequence just creates a wrapper for better filter and map actions, so it's better to do it this way
        return (this.declaredMembers + this.allSuperclasses.flatMap { kClass -> kClass.declaredMembers }).asSequence()
    }

/**
 * Unwraps the object referenced in a [KCallable].
 *
 * @receiver [KCallable] that you want to unwrap.
 *
 * @param R Type parameter of the [KCallable].
 * @param receiver Object containing the [KCallable] if it is non-static.
 *
 * @return The [R] referenced by the [KCallable].
 */
private fun <R> KCallable<R>.handleCall(receiver: Any): R {
    val accessible = this.isAccessible // Store accessibility
    this.isAccessible = true // Change accessibility
    return try {
        this.call(receiver) // If field is non-static
    } catch (e: IllegalArgumentException) {
        this.call() // If field is static
    } finally {
        this.isAccessible = accessible // Reset accessibility
    }
}

/**
 * Finds all [KCallable] fields in this class that reference [Listener] fields.
 *
 * @return A [Sequence] of [KCallable] objects that reference [Listener] fields.
 */
private inline val KClass<*>.listeners: Sequence<KCallable<Listener>>
    get() {
        @Suppress("UNCHECKED_CAST") // Should never throw an error
        return this.allMembers.filter { kCallable ->
            // Check if it has the annotation and check if the return type is a listener
            kCallable.hasAnnotation<EventHandler>() && kCallable.returnType.withNullability(false)
                .isSubtypeOf(typeOf<Listener>())
        } as Sequence<KCallable<Listener>>
    }

/**
 * Gets all [Listener] fields inside this object's class.
 *
 * @return An [Array] of [Listener] fields inside this object.
 */
internal val Any.listenerArray: Array<Listener>
    get() {
        val listeners = this::class.listeners.toList() // So we can index and access list.size

        return when (listeners.size) {
            0 -> arrayOf() // No listeners

            1 -> arrayOf(listeners[0].handleCall(this)) // 1 Listener

            2 -> arrayOf(listeners[0].handleCall(this), listeners[1].handleCall(this)) // 2 Listeners

            else -> { // More than 2 Listeners, could be optimized
                Array(listeners.size) { index ->
                    listeners[index].handleCall(this)
                }
            }
        }
    }

/**
 * Returns all [Listener] fields inside this object's class.
 *
 * @return A [List] of [Listener] fields inside this object
 */
internal val Any.listenerList: List<Listener>
    get() {
        return this::class.listeners.map { kCallable -> kCallable.handleCall(this) }.toList()
    }