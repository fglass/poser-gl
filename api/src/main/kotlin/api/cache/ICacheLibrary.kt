package api.cache

interface ICacheLibrary {
    val is317: Boolean
    val isHigherRev: Boolean

    fun getArchive(indexId: Int, archiveId: Int): ICacheArchive?
    fun getLastArchiveId(indexId: Int): Int
    fun addArchive(indexId: Int, archiveId: Int)
    fun update(indexId: Int, listener: ProgressListener)
    fun close()
}

interface ICacheArchive {
    val lastFileId: Int
    fun getFile(id: Int): ICacheFile?
    fun getFile(name: String): ICacheFile?
    fun addFile(id: Int, data: ByteArray)
    fun addFile(name: String, data: ByteArray)
}

interface ICacheFile {
    val id: Int
    val data: ByteArray?
}

interface ProgressListener {
    fun notify(progress: Double, message: String?)
}