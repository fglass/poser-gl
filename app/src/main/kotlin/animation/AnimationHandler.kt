package animation

import animation.command.Command
import animation.command.CommandHistory
import render.RenderContext

const val MAX_LENGTH = 999

class AnimationHandler(private val context: RenderContext) {

    var currentAnimation: Animation? = null
    val history = CommandHistory()
    var copiedFrame = Keyframe()
    private var previousFrame = Keyframe()

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

        animation.toggleItems(true)
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
            setCurrentFrame(0, 0) // Restart animation
        }

        if (playing) {
            if (--frameLength <= 0) { // Traverse frame
                frameCount++
                frameLength = animation.keyframes[getCurrentFrameIndex(animation)].length
            }
            timer = ++timer % animation.length // Increment timer
        }

        val keyframe = animation.keyframes[getCurrentFrameIndex(animation)]
        keyframe.apply(context)

        if (keyframe.id != previousFrame.id) {
            onNewFrame(keyframe)
        }
        context.gui.animationPanel.tickCursor(timer, animation.length)
    }

    fun getCurrentFrameIndex(animation: Animation): Int {
        return frameCount % animation.keyframes.size // TODO: use current animation?
    }

    private fun onNewFrame(keyframe: Keyframe) {
        context.nodeRenderer.reselectNode()
        context.gui.editorPanel.setKeyframe(keyframe)
        previousFrame = keyframe
    }

    fun getAnimationOrCopy(): Animation? {
        val current = currentAnimation ?: return null

        // Not original animation so no need to copy
        if (current.modified) {
            return current
        }

        // Copy animation as now modified
        val newIndex = context.cacheService.animations.maxBy { it.key }!!.key + 1
        val copied = Animation(newIndex, current)
        addAnimation(copied)
        copied.setRootNode()
        return copied
    }

    fun addAnimation(animation: Animation) {
        currentAnimation = animation
        context.cacheService.animations[animation.sequence.id] = animation
        context.cacheService.addFrameMap(animation)
        context.gui.listPanel.animationList.addElement(animation)
    }

    fun togglePlay() {
        setPlay(!playing)
    }

    fun setPlay(playing: Boolean) {
        if (playing && currentAnimation == null) {
            return
        }
        this.playing = playing
        context.gui.animationPanel.menu.updatePlayIcon(playing)
    }

    fun setCurrentFrame(frame: Int, offset: Int) {
        val animation = currentAnimation?: return
        frameCount = frame

        var cumulative = 0
        val frameIndex = getCurrentFrameIndex(animation)
        repeat(frameIndex) {
            cumulative += animation.keyframes[it].length
        }

        timer = cumulative + offset
        val keyframe = animation.keyframes[frameIndex]
        frameLength = keyframe.length - offset
    }

    fun executeCommand(command: Command) {
        if (command.execute()) {
            history.add(command)
        }
    }

    fun resetAnimation() {
        if (currentAnimation == null) {
            return
        }

        val previous = currentAnimation
        currentAnimation = null
        context.gui.listPanel.animationList.updateElement(previous)
        previous?.toggleItems(false)

        timer = 0
        frameCount = 0
        frameLength = 0
        previousFrame = Keyframe()

        history.reset()
        context.nodeRenderer.reset() // TODO: use events
        context.gui.editorPanel.reset()
        context.gui.animationPanel.reset()
    }
}