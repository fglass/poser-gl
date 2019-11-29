package animation.command

interface AnimationCommand {

    fun execute()

    fun unexecute()

    fun reversible(): Boolean
}
