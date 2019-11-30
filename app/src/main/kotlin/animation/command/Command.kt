package animation.command

interface Command {

    fun execute()

    fun unexecute()

    fun reversible(): Boolean
}
