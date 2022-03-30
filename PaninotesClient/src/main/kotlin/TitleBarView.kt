import borderless.BorderlessScene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignW

class TitleBarView(val scene: BorderlessScene, val stage: Stage, val htmlEditor: CustomHTMLEditor, val model: Model) :
    BorderPane() {

    private val titleLabel = Label("Paninotes")

    private val minimizeButton = Button()
    private val maximizeButton = Button()
    private val closeButton = Button()

    init {
        scene.setMoveControl(this)
        titleLabel.styleClass.add("title-label")

        val minimizeIcon = FontIcon(MaterialDesignW.WINDOW_MINIMIZE)
        val maximizeIcon = FontIcon(MaterialDesignW.WINDOW_MAXIMIZE)
        val restoreIcon = FontIcon(MaterialDesignW.WINDOW_RESTORE)
        val closeIcon = FontIcon(MaterialDesignW.WINDOW_CLOSE)

        minimizeIcon.iconSize = 14
        maximizeIcon.iconSize = 14
        restoreIcon.iconSize = 14
        closeIcon.iconSize = 14

        minimizeButton.graphic = minimizeIcon
        maximizeButton.graphic = maximizeIcon
        closeButton.graphic = closeIcon

        minimizeButton.styleClass.addAll(JMetroStyleClass.LIGHT_BUTTONS, "icon-button")
        maximizeButton.styleClass.addAll(JMetroStyleClass.LIGHT_BUTTONS, "icon-button")
        closeButton.styleClass.addAll(JMetroStyleClass.LIGHT_BUTTONS, "close-button")

        minimizeButton.setOnAction { scene.minimise() }
        maximizeButton.setOnAction { scene.maximise() }
        closeButton.setOnAction { StageUtils.saveOnClose(model, stage, htmlEditor) }

        stage.maximizedProperty().addListener { _, _, newVal ->
            if (newVal) maximizeButton.graphic = restoreIcon
            else maximizeButton.graphic = maximizeIcon
        }

        val buttons = HBox(minimizeButton, maximizeButton, closeButton)

        this.center = titleLabel
        this.right = buttons

        this.styleClass.add("front-pane")
    }
}