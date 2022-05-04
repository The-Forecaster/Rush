package me.austin.rush.type

abstract class Cancellable {
    var isCancelled = false

    fun cancel() {
        this.isCancelled = true
    }
}