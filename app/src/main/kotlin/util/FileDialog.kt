package util

import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog

object FileDialog {

    fun openFile(filters: List<String> = listOf(), folder: Boolean = false): String? {
        val defaultPath = System.getProperty("user.home")
        val filter = filters.joinToString(",") { it.replace("*.", "") }
        val outPath = MemoryUtil.memAllocPointer(1)

        val res = if (!folder) NativeFileDialog.NFD_OpenDialog(filter, defaultPath, outPath)
                  else NativeFileDialog.NFD_PickFolder(defaultPath, outPath)

        var path: String? = null
        if (res == NativeFileDialog.NFD_OKAY) {
            path = outPath.stringUTF8
            NativeFileDialog.nNFD_Free(outPath.get(0))
        }
        MemoryUtil.memFree(outPath)
        return path
    }

    fun saveFile(suffix: String): String? {
        val defaultPath = System.getProperty("user.home")
        val outPath = MemoryUtil.memAllocPointer(1)
        val res = NativeFileDialog.NFD_SaveDialog(suffix, defaultPath, outPath)

        var path: String? = null
        if (res == NativeFileDialog.NFD_OKAY) {
            path = outPath.stringUTF8
            NativeFileDialog.nNFD_Free(outPath.get(0))
        }

        MemoryUtil.memFree(outPath)
        return path
    }
}