package animation

import Processor
import com.google.common.collect.HashMultimap
import net.runelite.cache.definitions.FrameDefinition

// https://www.rune-server.ee/runescape-development/rs2-client/tutorials/340745-runescapes-rendering-animation-system.html

const val MAX_LENGTH = 999

class AnimationHandler(private val context: Processor) {

    val animations = HashMap<Int, Animation>()
    val frames: HashMultimap<Int, FrameDefinition> = HashMultimap.create()
    var cacheAnimations: Int

    var currentAnimation: Animation? = null
    var previousFrame = Keyframe(-1, -1)
    var copiedFrame = Keyframe(-1, -1)
    private var frameCount = 0
    private var frameLength = 0

    private var playing = false
    private var timer = 0

    init {
        AnimationLoader(context, this)
        cacheAnimations = animations.size
    }

    fun load(animation: Animation) {
        resetAnimation()
        animation.load()
        frameLength = animation.keyframes.first().length
        currentAnimation = animation

        playPause(true)
        context.gui.animationPanel.setTimeline()
    }

    fun tick() {
        val animation = currentAnimation
        if (animation == null || context.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            setFrame(0, 0) // Restart animation
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

        if (keyframe.id != previousFrame.id) {
            onNewFrame(keyframe)
        }
        context.gui.animationPanel.tickCursor(timer, animation.maximumLength)
    }

    fun getFrameIndex(animation: Animation): Int {
        return frameCount % animation.keyframes.size
    }

    fun transformNode(coordIndex: Int, newValue: Int) {
        if (!context.framebuffer.nodeRenderer.enabled) {
            return
        }

        val selected = context.framebuffer.nodeRenderer.selectedNode?: return
        val type = context.framebuffer.nodeRenderer.selectedType
        val child = selected.reference.group[type]?: return
        val id = child.id

        val animation = copyIfNecessary()?: return

        val keyframe = animation.keyframes[getFrameIndex(animation)]
        val transformation = keyframe.transformations.first { it.id == id }
        transformation.offset.setComponent(coordIndex, newValue)
    }

    fun modifyKeyframeLength(newLength: Int) {
        val animation = copyIfNecessary()?: return
        animation.modifyKeyframeLength(newLength)
        setFrame(frameCount, 0) // Restart frame
        context.gui.animationPanel.setTimeline()
    }

    private fun copyIfNecessary(): Animation? {
        val animation = currentAnimation?: return null

        return if (!animation.modified) {
            val newId = animations.size
            val newAnimation = Animation(newId, animation)

            currentAnimation = newAnimation
            animations[newId] = newAnimation
            context.gui.listPanel.animationList.addElement(newAnimation)
            context.gui.animationPanel.sequenceId.textState.text = newId.toString()
            newAnimation
        } else {
            animation
        }
    }

    private fun onNewFrame(keyframe: Keyframe) {
        context.framebuffer.nodeRenderer.reselectNode()
        context.gui.editorPanel.setKeyframe(keyframe)
        previousFrame = keyframe
    }

    fun togglePlay() {
        playPause(!playing)
    }

    fun playPause(playing: Boolean) {
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
        val frameIndex = getFrameIndex(animation)
        for (i in 0 until frameIndex) {
            cumulative += animation.keyframes[i].length
        }

        timer = cumulative + offset
        val keyframe = animation.keyframes[frameIndex]
        frameLength = keyframe.length - offset
        onNewFrame(keyframe)
    }

    fun resetAnimation() {
        currentAnimation = null
        timer = 0
        frameCount = 0
        frameLength = 0
        previousFrame = Keyframe(-1,-1)
        context.framebuffer.nodeRenderer.deselectNode()
        context.framebuffer.nodeRenderer.nodes.clear()
        context.gui.animationPanel.reset()
    }
}