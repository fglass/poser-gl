package util

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.tinyfd.TinyFileDialogs

object FileDialog {

    fun openFile(filters: List<String> = listOf(), defaultPath: String = ".", folder: Boolean = false): String? {
        val pattern = filters.toPointerBuffer()
        val path = if (!folder) TinyFileDialogs.tinyfd_openFileDialog("", defaultPath, pattern, "", false)
                   else TinyFileDialogs.tinyfd_selectFolderDialog("", defaultPath)
        MemoryUtil.memFree(pattern)
        return path
    }

    fun saveFile(suffix: String): String? {
        val pattern = listOf("").toPointerBuffer()
        val path = TinyFileDialogs.tinyfd_saveFileDialog("", "untitled.$suffix", pattern, "")
        MemoryUtil.memFree(pattern)
        return path
    }

    private fun List<String>.toPointerBuffer(): PointerBuffer {
        val pointer = MemoryUtil.memAllocPointer(size)
        forEach { pointer.put(MemoryUtil.memUTF8(it)) }
        pointer.flip()
        return pointer
    }
}