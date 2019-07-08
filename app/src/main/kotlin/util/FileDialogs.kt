package util

import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog

object FileDialogs {

    fun openFile(filters: List<String>, defaultPath: String): String? {
        val filter = filters.joinToString(",") { it.replace("*.", "") }
        val outPath = MemoryUtil.memAllocPointer(1)

        val res = NativeFileDialog.NFD_OpenDialog(filter, defaultPath, outPath)
        var path: String? = null

        if (res == NativeFileDialog.NFD_OKAY) {
            path = outPath.stringUTF8
            NativeFileDialog.nNFD_Free(outPath.get(0))
        }
        MemoryUtil.memFree(outPath)
        return path
    }

    fun saveFile(suffix: String, defaultPath: String): String? {
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