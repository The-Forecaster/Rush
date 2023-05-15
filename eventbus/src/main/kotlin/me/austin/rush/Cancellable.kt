package me.austin.rush

/**
 * Framework for a cancellable event
 *
 * @author Austin
 * @since 2022
 */
abstract class Cancellable {
    var isCancelled = false
        private set

    /**
     * Use this function to set isCancelled to true
     */
    fun cancel() {
        this.isCancelled = true
    }
}