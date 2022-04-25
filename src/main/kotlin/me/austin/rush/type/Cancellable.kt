package me.austin.rush.type

abstract class Cancellable {
    protected var cancelled = false

    fun cancel() {
        this.cancelled = true
    }

    fun isCancelled() = cancelled
}