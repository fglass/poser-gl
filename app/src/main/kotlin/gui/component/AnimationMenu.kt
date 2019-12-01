package gui.component

import render.SPRITE_PATH
import render.RenderContext
import org.joml.Vector2f
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import org.liquidengine.legui.style.length.Auto
import util.setSizeLimits

class AnimationMenu(context: RenderContext): Panel() {

    private val play: ImageButton
    private val playIcon = BufferedImage(SPRITE_PATH + "play.png")
    private val playHoveredIcon = BufferedImage(SPRITE_PATH + "play-hovered.png")
    private val pauseIcon = BufferedImage(SPRITE_PATH + "pause.png")
    private val pauseHoveredIcon = BufferedImage(SPRITE_PATH + "pause-hovered.png")
    private val nodeIcon = BufferedImage(SPRITE_PATH + "node.png")
    private val nodeToggledIcon = BufferedImage(SPRITE_PATH + "node-toggled.png")
    private val infoIcon = BufferedImage(SPRITE_PATH + "info.png")
    private val infoHoveredIcon = BufferedImage(SPRITE_PATH + "info-hovered.png")

    init {
        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        style.flexStyle.flexGrow = 1
        style.position = Style.PositionType.RELATIVE
        style.setMaxHeight(23f)

        style.background.color = ColorConstants.darkGray()
        style.border.isEnabled = false
        style.setBorderRadius(0f)

        play = ImageButton(Vector2f(), playIcon)
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                context.animationHandler.togglePlay()
            }
        }
        play.style.setMargin(4f, 0f, 0f, 10f)
        play.setSizeLimits(17f, 17f)
        add(play)

        val infoButton = ImageButton(Vector2f(), infoIcon, "Information")
        infoButton.hoveredIcon = infoHoveredIcon
        infoButton.setSizeLimits(22f, 21f)
        infoButton.style.setMargin(3f, 0f, 0f, 28f)
        infoButton.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                val animation = context.animationHandler.currentAnimation?: return@addListener
                SequenceDialog(context, animation).display()
            }
        }
        add(infoButton)

        val nodeToggle = ImageButton(Vector2f(), nodeIcon, "Skeleton")
        nodeToggle.setSizeLimits(19f, 18f)
        nodeToggle.style.setMargin(3f, 0f, 0f, 51f)
        nodeToggle.style.setBorderRadius(1f)
        nodeToggle.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.button == Mouse.MouseButton.MOUSE_BUTTON_LEFT &&
                event.action == MouseClickEvent.MouseClickAction.CLICK) {
                nodeToggle.setIconImage(if (nodeToggle.image == nodeToggledIcon) nodeIcon else nodeToggledIcon)
                context.nodeRenderer.toggle()
            }
        }
        add(nodeToggle)
    }

    fun updatePlayIcon(playing: Boolean) {
        if (playing) {
            play.setIconImage(pauseIcon)
            play.hoveredIcon = pauseHoveredIcon
        } else {
            play.setIconImage(playIcon)
            play.hoveredIcon = playHoveredIcon
        }
    }
}