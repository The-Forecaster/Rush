package me.austin.rush

/**
 * Framework for a cancellable event.
 *
 * @author Austin
 * @since 2022
 */
interface Cancellable {
    /**
     * If the event has been cancelled or not.
     *
     * If an event is cancelled then it will not be posted to listeners after it has been cancelled.
     */
    var isCancelled: Boolean

    /**
     * Use this function to set isCancelled to true.
     */
    fun cancel() {
        this.isCancelled = true
    }
}