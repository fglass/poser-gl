package animation.command

interface KeyframeCommand {

    fun execute()

    fun unexecute()

    fun reversible(): Boolean
}