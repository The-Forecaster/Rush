package me.austin.light

import kotlin.reflect.KClass

/**
 * For containing a listener and the object it's contained inside
 *
 * @param lambda action to be invoked
 * @param parent object to be checked when removing listeners
 */
open class Handler(
    lambda: (Nothing) -> Unit,
    /**
     * (Nullable) object to check when removing listeners
     */
    private val parent: Any? = null
) {
    /**
     * Action to be invoked
     */
    // This cast will never fail
    open val action = lambda as (Any) -> Unit
}

/**
 * Creates a new lambda with the parent object as its [Handler.parent]
 */
fun <T> Any.handler(lambda: (T) -> Unit): Handler {
    return Handler(lambda, this)
}

/**
 * Extremely lightweight event-bus made for kotlin use
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive if the bus will also post superclasses of events posted
 */
open class EventManager(open val recursive: Boolean = true) {
    /**
     * For all the classes of events and the lambdas which target them
     */
    open val registry = mutableMapOf<KClass<*>, Array<Handler>>()

    /**
     * This is so we only ever have 1 write action going on at a time
     */
    open val writeSync = Any()

    /**
     * Add a static lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T> register(noinline action: (T) -> Unit) {
        synchronized(writeSync) {
            val array = this.registry[T::class]

            if (array == null) {
                this.registry[T::class] = arrayOf(Handler(action))
            } else if (!array.contains(Handler(action))) {
                val fin = arrayOfNulls<Handler>(array.size + 1)

                System.arraycopy(array, 0, fin, 0, array.size)
                array[array.size - 1] = Handler(action)

                this.registry[T::class] = fin as Array<Handler>
            }
        }
    }

    /**
     * Add a [Handler] to the [registry]
     *
     * @param T the action the handler will accept
     * @param handler the handler object to add to the registry
     */
    inline fun <reified T> register(handler: Handler) {
        synchronized(writeSync) {
            val array = this.registry[T::class]

            if (array == null) {
                this.registry[T::class] = arrayOf(handler)
            } else if (!array.contains(handler)) {
                val fin = arrayOfNulls<Handler>(array.size + 1)

                System.arraycopy(array, 0, fin, 0, array.size)
                array[array.size - 1] = handler

                this.registry[T::class] = fin as Array<Handler>
            }
        }
    }

    /**
     * Remove all [Handler] objects from the list that have the parameter as their parent
     *
     * @param handler the object search for and remove listeners of
     */
    inline fun <reified T> unregister(handler: Handler) {
        synchronized(writeSync) {
            this.registry[T::class]?.let { handlers ->
                if (handlers.contains(handler)) {
                    if (handlers.size == 1) {
                        this.registry.remove(T::class)
                    } else {
                        val list = arrayOfNulls<Handler>(handlers.size - 1)
                        var index = 0

                        for (element in handlers) {
                            if (handler != element) {
                                list[index] = handler
                                index += 1
                            }
                        }

                        this.registry[T::class] = list as Array<Handler>
                    }
                }
            }
        }
    }

    /**
     * For dispatching events
     *
     * @param event event to be posted to all registered actions
     */
    open fun post(event: Any) {
        for (clazz in event::class.getClasses(this.recursive)) {
            this.registry[clazz]?.let {
                for (handler in it) {
                    handler.action(event)
                }
            }
        }
    }

    /**
     * This is copied from [<a href="https://github.com/x4e/EventDispatcher/">cookiedragon</a>]
     * @param recursive if we should also include superclasses of the class
     * @return immutable list of the class and all of its superclasses in order if [recursive] is set to true
     */
    private fun KClass<*>.getClasses(recursive: Boolean): List<KClass<*>> {
        val classes = mutableListOf(this)
        var clazz: Class<*>? = this.java.superclass
        while (recursive && clazz != null) {
            classes.add(clazz.kotlin)
            clazz = clazz.superclass
        }
        return classes.toList()
    }
}