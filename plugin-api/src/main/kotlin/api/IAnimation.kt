package api

import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.progress.AbstractProgressListener

interface IAnimation {

    fun toSequence(archiveId: Int): SequenceDefinition

    fun getKeyframes(): List<IKeyframe>
}

interface IKeyframe {

    fun encode(id: Int = -1, osrs: Boolean = true): ByteArray

    fun isModified(): Boolean

    fun getFrameMapDef(): FramemapDefinition
}

abstract class ProgressListenerWrapper: AbstractProgressListener()