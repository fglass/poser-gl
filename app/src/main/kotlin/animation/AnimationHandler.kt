package animation

import Processor
import mu.KotlinLogging
import net.runelite.cache.definitions.FramemapDefinition

const val MAX_LENGTH = 999
private val logger = KotlinLogging.logger {}

class AnimationHandler(private val context: Processor) {

    var currentAnimation: Animation? = null
    var copiedFrame = Keyframe(-1, -1, -1, FramemapDefinition())
    private var previousFrame = Keyframe(-1, -1, -1, FramemapDefinition())
    private var frameLength = 0
    var frameCount = 0

    private var playing = false
    private var timer = 0

    fun load(animation: Animation) {
        resetAnimation()
        animation.load()

        if (animation.keyframes.isEmpty()) {
            return
        }

        frameLength = animation.keyframes.first().length
        currentAnimation = animation
        setPlay(true)
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
            timer = ++timer % animation.length // Increment timer
        }

        val keyframe = animation.keyframes[getFrameIndex(animation)]
        keyframe.apply(context)

        if (keyframe.id != previousFrame.id) {
            onNewFrame(keyframe)
        }
        context.gui.animationPanel.tickCursor(timer, animation.length)
    }

    fun getFrameIndex(animation: Animation): Int {
        return frameCount % animation.keyframes.size
    }

    private fun onNewFrame(keyframe: Keyframe) {
        context.nodeRenderer.reselectNode()
        context.gui.editorPanel.setKeyframe(keyframe)
        previousFrame = keyframe
    }

    fun transformNode(coordIndex: Int, newValue: Int) {
        if (!context.nodeRenderer.enabled) {
            return
        }

        val selected = context.nodeRenderer.selectedNode?: return
        val type = context.nodeRenderer.selectedType
        val preCopy = selected.getTransformation(type)?: return

        val animation = getAnimation(false)?: return
        val keyframe = animation.keyframes[getFrameIndex(animation)]

        try {
            val transformation = keyframe.transformations.first { it.id == preCopy.id }
            transformation.delta.setComponent(coordIndex, newValue)
            keyframe.modified = true
        } catch (e: NoSuchElementException) {
            logger.error(e) { "Node ${preCopy.id} does not exist" }
        }
    }

    fun getAnimation(useCurrent: Boolean): Animation? {
        val current = currentAnimation?: return null

        // Not original animation so no need to copy
        if (current.modified || useCurrent) {
            return current
        }

        val newIndex = context.cacheService.animations.maxBy { it.key }!!.key + 1
        val copied = Animation(newIndex, current)

        currentAnimation = copied
        context.cacheService.animations[newIndex] = copied
        context.gui.listPanel.animationList.addElement(copied)
        context.gui.animationPanel.sequenceId.textState.text = newIndex.toString()
        return copied
    }

    fun togglePlay() {
        setPlay(!playing)
    }

    fun setPlay(playing: Boolean) {
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
    }

    fun resetAnimation() {
        if (currentAnimation != null) {
            currentAnimation = null
            timer = 0
            frameCount = 0
            frameLength = 0
            previousFrame = Keyframe(-1, -1, -1, FramemapDefinition())
            context.nodeRenderer.reset()
            context.gui.editorPanel.reset()
            context.gui.animationPanel.reset()
        }
    }
}