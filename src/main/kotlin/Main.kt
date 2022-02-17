
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage
import org.jsoup.Jsoup

class Main : Application() {

    override fun start(stage: Stage) {
        // create the root of the scene graph
        // BorderPane supports placing children in regions around the screen
        val model = Model()

        // Initialize all widgets--------------------------------------------------------------------------------------------
        val layout = BorderPane()

        val htmlEditor = HTMLEditor()
        val topMenuView = TopMenuView(model,htmlEditor,stage)
        val sideNotebookPane = SideNotebookPaneView(model)
        val sideIconPane = SideIconPaneView(model, sideNotebookPane)

        // Hacky thing so when the notebook pane is not visible, it doesn't take up any empty space in the side pane
        sideNotebookPane.managedProperty().bind(sideNotebookPane.visibleProperty());

        htmlEditor.htmlText = "Hello <a href=\"https://github.com/TestFX/TestFX\">world</a>"

        model.addView(topMenuView)
<<<<<<< HEAD
        val noHtmlTags = Jsoup.parse(htmlEditor.htmlText).text()
        println(noHtmlTags)

        model.addView(sideNotebookPane)
        model.addView(sideIconPane)

        // hardcode some notes and notebooks for testing
        val notebook1 = model.createNotebook("notebook1")
        notebook1.addNote(Note("note11"))
        notebook1.addNote(Note("note12"))
        val notebook2 = model.createNotebook("notebook2")
        notebook2.addNote(Note("note21"))
        notebook2.addNote(Note("note22"))
        notebook2.addNote(Note("note23"))
        model.addNotebook(notebook1)
        model.addNotebook(notebook2)

>>>>>>> master
        // build the scene graph
        val sidePane = HBox()
        sidePane.children.addAll(sideIconPane, sideNotebookPane)

        layout.top = topMenuView
        layout.center = htmlEditor
        layout.left = sidePane
        layout.padding = Insets(5.0)

        // create and show the scene
        val scene = Scene(layout)

        stage.width = 800.0
        stage.height = 500.0
        stage.scene = scene
        stage.isResizable = true
        stage.title = "Paninotes"

        stage.show()
    }
}