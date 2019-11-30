package animation.command

class CommandHistory : ArrayList<Command>() {

    private var index = 0

    override fun add(element: Command): Boolean {
        if (element.reversible()) {
            super.add(index++, element) // TODO: replacing
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
}