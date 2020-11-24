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

        animation.toggleItems(equip = true)
        frameLength = animation.keyframes.first().length
        currentAnimation = animation

        setPlay(true)
        context.gui.animationPanel.setTimeline()
    }

    fun tick() {
        val animation = currentAnimation
        if (animation == null || context.entityHandler.entity == null) {
            return
        }

        if (timer > MAX_LENGTH) {
            setCurrentFrame(0) // Restart animation
        }

        if (playing) {
            if (--frameLength <= 0) { // Traverse frame
                frameCount++
                frameLength = animation.keyframes[animation.getFrameIndex(frameCount)].length
            }
            timer = ++timer % animation.length // Increment timer
        }

        val keyframe = animation.keyframes[animation.getFrameIndex(frameCount)]
        context.nodeRenderer.rootNode = keyframe.rootNode
        keyframe.apply(context) // TODO: not when paused but keep rendering nodes

        if (keyframe.id != previousFrame.id) {
            onNewFrame(keyframe)
        }

        context.gui.animationPanel.tickCursor(timer, animation.length)
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
        val maxId = context.cacheService.animations.keys.maxOrNull() ?: error("No animations")
        val copied = Animation(maxId + 1, current)
        addAnimation(copied)
        return copied
    }

    fun addAnimation(animation: Animation) {
        currentAnimation = animation
        context.cacheService.animations[animation.sequence.id] = animation
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

    fun setCurrentFrame(frame: Int, offset: Int = 0) {
        val animation = currentAnimation ?: return
        frameCount = animation.getFrameIndex(frame)

        var cumulative = 0
        val frameIndex = animation.getFrameIndex(frameCount)
        repeat(frameIndex) {
            cumulative += animation.keyframes[it].length
        }

        timer = cumulative + offset
        val keyframe = animation.keyframes[frameIndex]
        frameLength = keyframe.length - offset
    }

    fun setNextFrame() {
        setCurrentFrame(++frameCount)
    }

    fun setPreviousFrame() {
        setCurrentFrame(--frameCount)
    }

    fun executeCommand(command: Command) {
        if (command.execute()) {
            history.add(command)
        }
    }

    fun resetAnimation() { // TODO: use events
        if (currentAnimation == null) {
            return
        }

        val previous = currentAnimation
        currentAnimation = null
        context.gui.listPanel.animationList.updateElement(previous)
        previous?.toggleItems(equip = false)

        timer = 0
        frameCount = 0
        frameLength = 0
        previousFrame = Keyframe()

        history.reset()
        context.nodeRenderer.reset()
        context.gui.editorPanel.reset()
        context.gui.animationPanel.reset()
    }
}