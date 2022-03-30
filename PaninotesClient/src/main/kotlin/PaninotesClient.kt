import borderless.BorderlessScene
import fr.brouillard.oss.cssfx.CSSFX
import javafx.application.Application
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.io.File
import java.nio.file.Paths

class PaninotesClient : Application() {

    private val LIGHT_STYLESHEET_URL = PaninotesClient::class.java.getResource("css/light.css")?.toExternalForm()
    private val DARK_STYLESHEET_URL = PaninotesClient::class.java.getResource("css/dark.css")?.toExternalForm()
    private val BASE_DIRECTORY = File(Paths.get(System.getProperty("user.home"), ".paninotes").toUri())

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(PaninotesClient::class.java, *args)
        }
    }

    override fun start(stage: Stage) {
        if (!BASE_DIRECTORY.exists()) BASE_DIRECTORY.mkdir()

        // activate css live update
        CSSFX.start()

        val jMetro = JMetro()

        // create the root of the scene graph
        // BorderPane supports placing children in regions around the screen
        val layout = BorderPane()

        // create borderless scene
        val scene = BorderlessScene(stage, layout)
//        if (!System.getProperty("os.name").contains("Windows")) scene.isSnapEnabled = false

        // Initialize all widgets--------------------------------------------------------------------------------------------
        val model = Model(stage)
        model.initializeNotebooks()

        val htmlEditor = CustomHTMLEditor()
        htmlEditor.stage = stage
        val titleBarView = TitleBarView(scene, stage, htmlEditor, model)
        val topMenuView = TopMenuView(model, htmlEditor, stage, jMetro)
        val noteTabsView = NoteTabsView(model, htmlEditor, stage)
        val sideNotebookPane = SideNotebookPaneView(model, htmlEditor, stage)
        val sideIconPane = SideIconPaneView(model, htmlEditor, sideNotebookPane, stage)

        // Hacky thing so when the notebook pane is not visible, it doesn't take up any empty space in the side pane
        sideNotebookPane.managedProperty().bind(sideNotebookPane.visibleProperty())

        model.addView(topMenuView)
        model.addView(noteTabsView)
        model.addView(sideNotebookPane)
        model.addView(sideIconPane)
        model.notifyViews()

        // build the scene graph
        val sidePane = HBox()
        sidePane.children.addAll(sideIconPane, sideNotebookPane)

        val topPane = VBox()
        topPane.children.addAll(titleBarView, topMenuView, noteTabsView)

        layout.top = topPane
        layout.center = htmlEditor
        layout.left = sidePane

        // apply jmetro
        jMetro.scene = scene
        layout.styleClass.add(JMetroStyleClass.BACKGROUND)

        // apply config
        stage.width = Config.width
        stage.height = Config.height
        stage.x = Config.x
        stage.y = Config.y
        if (Config.isMaximized) scene.maximise()
        if (Config.darkTheme) {
            jMetro.style = Style.DARK
            jMetro.scene.stylesheets.add(DARK_STYLESHEET_URL)
        } else {
            jMetro.style = Style.LIGHT
            jMetro.scene.stylesheets.add(LIGHT_STYLESHEET_URL)
        }

        // set listeners for config
        stage.widthProperty().addListener { _, _, newVal -> Config.width = newVal as Double }
        stage.heightProperty().addListener { _, _, newVal -> Config.height = newVal as Double }
        stage.xProperty().addListener { _, _, newVal -> Config.x = newVal as Double }
        stage.yProperty().addListener { _, _, newVal -> Config.y = newVal as Double }
        stage.maximizedProperty().addListener { _, _, newVal -> Config.isMaximized = newVal }
        jMetro.styleProperty().addListener { _, _, newVal -> Config.darkTheme = (newVal == Style.DARK) }

        // save config on close
        stage.setOnHiding { Config.saveConfig() }

        stage.minWidth = 890.0
        stage.minHeight = 700.0
        stage.scene = scene
        stage.isResizable = true
        stage.title = "Paninotes"

        stage.show()

        // add the custom buttons to the HTML editor
        // the HTML editor toolbar buttons aren't initialized on construction, only after a call to layoutChildren
        // so, after stage.show(), by now, the editor toolbar should be populated, so we can add our custom buttons whereever we want
        htmlEditor.addCustomButtons()
    }
}