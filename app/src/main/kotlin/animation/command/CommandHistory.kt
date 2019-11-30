package animation.command

class CommandHistory : ArrayList<Command>() {

    private var index = 0

    override fun add(element: Command): Boolean {
        if (element.reversible()) {
            super.add(index++, element)
            removeRange(index, size) // Set new command as head
        }
        return true
    }

    fun undo() {
        if (index > 0) {
            val command = get(--index)
            command.unexecute()
        }
    }

    fun redo() {
        if (index < size) {
            val command = get(index++)
            command.execute()
        }
    }

    fun reset() {
        index = 0
        clear()
    }
}