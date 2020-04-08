package cache

import api.cache.ICacheArchive
import api.cache.ICacheFile
import api.cache.ICacheLibrary
import api.cache.ProgressListener
import com.displee.cache.index.archive.Archive
import com.displee.cache.index.archive.file.File
import com.displee.cache.CacheLibrary as DispleeCacheLibrary
import com.displee.cache.ProgressListener as DispleeProgressListener

class CacheLibrary(path: String) : ICacheLibrary {

    private val library = DispleeCacheLibrary(path)

    override val is317 = library.is317()

    override val isHigherRev = !library.is317() && !library.isOSRS()

    override fun getArchive(indexId: Int, archiveId: Int): ICacheArchive? {
        val archive = library.index(indexId).archive(archiveId) ?: return null
        return CacheArchive(archive)
    }

    override fun getLastArchiveId(indexId: Int) = library.index(indexId).last()?.id ?: -1

    override fun addArchive(indexId: Int, archiveId: Int) {
        library.index(indexId).add(archiveId, overwrite = false)
    }

    override fun update(indexId: Int, listener: ProgressListener) {
        library.index(indexId).update(listener as DispleeProgressListener)
    }

    override fun close() {
        library.close()
    }
}

class CacheArchive(private val archive: Archive) : ICacheArchive {

    override val lastFileId = archive.last()?.id ?: -1

    override fun getFile(id: Int) = CacheFile(archive.file(id))

    override fun getFile(name: String) = CacheFile(archive.file(name))

    override fun addFile(id: Int, data: ByteArray) {
        archive.add(id, data)
    }

    override fun addFile(name: String, data: ByteArray) {
        archive.add(name, data)
    }
}

class CacheFile(file: File?) : ICacheFile {
    override val id = file?.id ?: -1
    override val data = file?.data
}