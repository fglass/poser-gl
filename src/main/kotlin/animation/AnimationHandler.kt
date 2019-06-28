package animation

import Processor
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition
import net.runelite.cache.definitions.SequenceDefinition

// https://www.rune-server.ee/runescape-development/rs2-client/tutorials/340745-runescapes-rendering-animation-system.html

const val MAX_LENGTH = 999

class AnimationHandler(private val context: Processor) {

    val sequences = HashMap<Int, SequenceDefinition>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()

    var currentAnimation: Animation? = null
    var currentFrame = Keyframe(-1, -1)
    var copiedFrame = Keyframe(-1, -1)
    private var frameLength = 0
    private var frameCount = 0

    private var playing = false
    private var timer = 0

    init {
        AnimationLoader(this)
    }

    fun load(sequence: SequenceDefinition) {
        resetAnimation()

        val animation = Animation(context, sequence, frames)
        frameLength = animation.keyframes.first().length
        currentAnimation = animation

        isPlaying(true)
        context.gui.animationPanel.setTimeline()
    }

    fun tick() {
        val animation = currentAnimation
        if (animation == null || context.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            restartAnimation()
        }

        if (playing) {
            if (--frameLength <= 0) { // Traverse frame
                frameCount++
                frameLength = animation.keyframes[getFrameIndex(animation)].length
            }
            timer = ++timer % animation.maximumLength // Increment timer
        }

        val keyframe = animation.keyframes[getFrameIndex(animation)]
        keyframe.apply(context)

        if (keyframe.id != currentFrame.id) {
            onNewFrame(keyframe)
        }
        context.gui.animationPanel.tickCursor(timer, animation.maximumLength)
    }

    fun getFrameIndex(animation: Animation): Int {
        return frameCount % animation.keyframes.size
    }

    fun transformNode(coordIndex: Int, newValue: Int) {
        val selected = context.framebuffer.nodeRenderer.selectedNode ?: return

        val type = context.framebuffer.nodeRenderer.selectedType
        val child = selected.reference.group[type]?: return
        val id = child.id

        val animation = currentAnimation?: return
        val keyframe = animation.keyframes[getFrameIndex(animation)]
        val transformation = keyframe.transformations.first { it.id == id }
        transformation.offset.setComponent(coordIndex, newValue)
    }

    private fun onNewFrame(keyframe: Keyframe) {
        context.framebuffer.nodeRenderer.reselectNode()
        context.gui.editorPanel.setKeyframe(keyframe)
        currentFrame = keyframe
    }

    fun togglePlay() {
        isPlaying(!playing)
    }

    fun isPlaying(playing: Boolean) {
        if (playing && currentAnimation == null) {
            return
        }
        this.playing = playing
        context.gui.animationPanel.updatePlayIcon(playing)
    }

    fun setFrame(frame: Int, offset: Int) {
        val animation = currentAnimation?: return
        frameCount = frame

        var cumulative = 0
        for (i in 0 until frameCount) {
            cumulative += animation.keyframes[i].length
        }

        timer = cumulative + offset
        frameLength = animation.keyframes[frame].length - offset
    }

    fun restartFrame() {
        val animation = currentAnimation?: return
        animation.maximumLength = animation.getMaxLength()
        setFrame(frameCount, 0)
    }

    private fun restartAnimation() {
        setFrame(0, 0)
    }

    fun resetAnimation() {
        currentAnimation = null
        timer = 0
        frameCount = 0
        frameLength = 0
        context.framebuffer.nodeRenderer.deselectNode()
        context.framebuffer.nodeRenderer.nodes.clear()
        context.gui.animationPanel.reset()
    }
}