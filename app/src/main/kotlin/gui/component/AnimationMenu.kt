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

class AnimationMenu(context: Processor, width: Float): Panel() {

    val sequenceId: Label
    private val play: ImageButton
    //private val importButton: ImageButton
    //private val exportButton: ImageButton
    val packButton: ImageButton
    private val nodeToggle: ToggleButton

    private val playIcon = BufferedImage(SPRITE_PATH + "play.png")
    private val pauseIcon = BufferedImage(SPRITE_PATH + "pause.png")
    private val nodeIcon = BufferedImage(SPRITE_PATH + "nodes.png")

    init {
        val x = 12f
        position = Vector2f(0f, 0f)
        size = Vector2f(width, 23f)

        style.background.color = ColorConstants.darkGray()
        style.setBorderRadius(0f)
        //style.border.isEnabled = false

        play = ImageButton(Vector2f(x, 8f), playIcon, "")
        play.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                context.animationHandler.togglePlay()
            }
        }
        play.size = Vector2f(10f, 10f)
        add(play)

        val sequence = Label("Sequence:", x + 14, 5f, 50f, 15f)
        sequenceId = Label("N/A", x + 73, 5f, 50f, 15f)
        add(sequence)
        add(sequenceId)

        style.display = Style.DisplayType.FLEX
        style.flexStyle.flexDirection = FlexStyle.FlexDirection.ROW
        style.flexStyle.flexGrow = 1
        style.flexStyle.flexShrink = 1
        style.flexStyle.justifyContent = FlexStyle.JustifyContent.CENTER
        style.flexStyle.alignItems = FlexStyle.AlignItems.CENTER
        style.position = Style.PositionType.RELATIVE
        style.maxHeight = 23f

        //importButton = addMenuButton("import", 110, context.importManager::import)
        //exportButton = addMenuButton("export", 85, context.exportManager::openDialog)
        packButton = addMenuButton("pack", 60, context.cacheService::pack)

        packButton.style.setMinimumSize(26f, 26f)
        packButton.style.setMaximumSize(26f, 26f)

        packButton.style.setMargin(0f, 0f, 0f, 5f)
        packButton.style.flexStyle.flexGrow = 1
        packButton.style.position = Style.PositionType.RELATIVE
        add(packButton)

        nodeToggle = ToggleButton(Vector2f(size.x - 32, 3f), Vector2f(20f, 20f), nodeIcon, "Skeleton", false)
        nodeToggle.style.setBorderRadius(1f)
        nodeToggle.listenerMap.addListener(MouseClickEvent::class.java) {
            context.nodeRenderer.enabled = !context.nodeRenderer.enabled
        }
        add(nodeToggle)
    }

    private fun addMenuButton(name: String, xOffset: Int, action: () -> Unit): ImageButton {
        val button = ImageButton(Vector2f(size.x - xOffset, 0f), BufferedImage("$SPRITE_PATH$name.png"), name.capitalize())
        button.size = Vector2f(26f, 26f)
        button.listenerMap.addListener(MouseClickEvent::class.java) { event ->
            if (event.action == MouseClickEvent.MouseClickAction.CLICK) {
                action.invoke()
            }
        }
        button.addHover(BufferedImage("$SPRITE_PATH$name-hovered.png"))
        add(button)
        return button
    }

    fun updatePlayIcon(playing: Boolean) {
        play.image = if (playing) pauseIcon else playIcon
    }
}