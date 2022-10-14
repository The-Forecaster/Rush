package me.austin.rush

abstract class Cancellable {
    var isCancelled = false

    fun cancel() {
        this.isCancelled = true
    }
}