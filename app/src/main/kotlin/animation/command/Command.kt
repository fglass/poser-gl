package animation.command

interface Command {

    fun execute(): Boolean

    fun unexecute()

    fun reversible(): Boolean
}
