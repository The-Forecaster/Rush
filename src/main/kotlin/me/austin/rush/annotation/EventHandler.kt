package me.austin.rush.annotation

const val DEFAULT = -50
const val LOWEST = -200
const val LOW = -100
const val MEDIUM = 0
const val HIGH = 100
const val HIGHEST = 200

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class EventHandler(
    /**
     * Priority of the listener
     */
    val priority: Int = DEFAULT
)