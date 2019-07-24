package gui.component

import SPRITE_PATH
import Processor
import org.joml.Vector2f
import org.liquidengine.legui.component.Label
import org.liquidengine.legui.component.Panel
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.image.BufferedImage
import org.liquidengine.legui.style.Style
import org.liquidengine.legui.style.color.ColorConstants
import org.liquidengine.legui.style.flex.FlexStyle
import org.liquidengine.legui.style.length.Auto
import org.liquidengine.legui.style.length.LengthType.PIXEL
import util.setSizeLimits

class AnimationMenu(context: Processor): Panel() {

    val sequenceId: Label
    private val play: ImageButton
    private val playIcon = BufferedImage(SPRITE_PATH + "play.png")
    private val pauseIcon = BufferedImage(SPRITE_PATH + "pause.png")
    private val nodeIcon = BufferedImage(SPRITE_PATH + "nodes.png")

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
        play.style.setMargin(8f, 0f, 0f, 12f)
        play.setSizeLimits(10f, 10f)
        add(play)

        val sequence = Label("Sequence:")
        sequence.style.setMargin(5f, 0f, 0f, 26f)
        sequence.setSizeLimits(50f, 15f)
        add(sequence)

        sequenceId = Label("N/A")
        sequenceId.style.setMargin(5f, 0f, 0f, 85f)
        sequenceId.setSizeLimits(50f, 15f)
        add(sequenceId)

        val importButton = addMenuButton("import", context.importManager::import)
        importButton.style.marginLeft = Auto.AUTO

        addMenuButton("export", context.exportManager::openDialog)
        addMenuButton("pack", context.cacheService::pack)

        val nodeToggle = ToggleButton(nodeIcon, "Skeleton", false)
        nodeToggle.style.position = Style.PositionType.RELATIVE
        nodeToggle.style.setMargin(PIXEL.length(3f), PIXEL.length(13f), PIXEL.length(0f), PIXEL.length(0f))
        nodeToggle.setSizeLimits(20f, 20f)

        nodeToggle.style.setBorderRadius(1f)
        nodeToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.nodeRenderer.enabled = !context.nodeRenderer.enabled
        }
        add(nodeToggle)
    }

    private fun addMenuButton(name: String, action: () -> Unit): ImageButton {
        val button = ImageButton(Vector2f(), BufferedImage("$SPRITE_PATH$name.png"), name.capitalize())
        button.addHover(BufferedImage("$SPRITE_PATH$name-hovered.png"))

        button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                action.invoke()
            }
        }

        button.setSizeLimits(26f, 26f)
        button.style.position = Style.PositionType.RELATIVE
        add(button)
        return button
    }

    fun updatePlayIcon(playing: Boolean) {
        play.image = if (playing) pauseIcon else playIcon
    }
}