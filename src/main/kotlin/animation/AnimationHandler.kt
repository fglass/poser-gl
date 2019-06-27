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

    private var currentAnimation: Animation? = null
    private var frameCount = 0
    private var frameLength = 0
    private var previousFrame = Keyframe(-1, -1)

    private var playing = false
    private var timer = 0

    init {
        AnimationLoader(this)
    }

    fun load(sequence: SequenceDefinition) {
        currentAnimation = Animation(sequence, frames)
        reset()
        isPlaying(true)
        context.framebuffer.nodeRenderer.deselectNode()
        context.gui.animationPanel.loadSequence(sequence)
    }

    fun togglePlay() {
        isPlaying(!playing)
    }

    fun isPlaying(playing: Boolean) {
        this.playing = playing
        context.gui.animationPanel.updatePlayIcon(playing)
    }

    private fun reset() {
        setFrame(0, 0, 0)
    }

    fun setFrame(time: Int, frame: Int, offset: Int) {
        timer = time
        frameCount = frame
        frameLength = currentAnimation!!.keyframes[frame].length - offset
    }

    fun tick() {
        if (currentAnimation == null || context.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            reset()
        }

        if (playing) {
            // Adjust timer
            if (getFrameIndex() == 0 && frameLength <= 0) { // Animation restarted
                timer = 0
            } else {
                timer++
            }

            // Traverse frame
            if (frameLength-- <= 0) {
                frameCount++
                frameLength = currentAnimation!!.keyframes[getFrameIndex()].length
            }
        }

        val keyframe = currentAnimation!!.keyframes[getFrameIndex()]
        keyframe.apply(context)

        if (keyframe.id != previousFrame.id) {
            onNewFrame()
            previousFrame = keyframe
        }
        context.gui.animationPanel.tickCursor(timer)
    }

    private fun getFrameIndex(): Int {
        return frameCount % currentAnimation!!.keyframes.size
    }

    private fun onNewFrame() {
        context.framebuffer.nodeRenderer.reselectNode()
    }

    fun transformNode(coordIndex: Int, newValue: Int) {
        val selected = context.framebuffer.nodeRenderer.selectedNode ?: return

        val type = context.framebuffer.nodeRenderer.selectedType
        val child = selected.reference.group[type]?: return
        val id = child.id

        val keyframe = currentAnimation!!.keyframes[getFrameIndex()]
        val transformation = keyframe.transformations.first { it.id == id }
        transformation.offset.setComponent(coordIndex, newValue)
    }

    fun resetAnimation() {
        currentAnimation = null
        frameCount = 0
        frameLength = 0
        context.framebuffer.nodeRenderer.deselectNode()
        context.framebuffer.nodeRenderer.clearNodes()
        context.gui.animationPanel.stop()
    }
}