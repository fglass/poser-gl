package api

import net.runelite.cache.definitions.FramemapDefinition
import net.runelite.cache.definitions.SequenceDefinition
import org.displee.progress.AbstractProgressListener
import org.joml.Vector3i

interface IAnimation {
    val keyframes: List<IKeyframe>
    fun toSequence(archiveId: Int): SequenceDefinition
}

interface IKeyframe {
    val modified: Boolean
    val frameMap: FramemapDefinition
    val transformations: List<ITransformation>
}

interface ITransformation {
    val id: Int
    val delta: Vector3i
}

abstract class ProgressListenerWrapper: AbstractProgressListener() // TODO: remove

fun getMask(delta: Vector3i): Int {
    val x = if (delta.x != 0) 1 else 0
    val y = if (delta.y != 0) 2 else 0
    val z = if (delta.z != 0) 4 else 0
    return x or y or z
}