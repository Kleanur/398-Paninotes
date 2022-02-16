
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
        // create the root of the scene graph
        // BorderPane supports placing children in regions around the screen

        // Initialize all widgets--------------------------------------------------------------------------------------------
        val layout = BorderPane()
        val topMenuView = TopMenuView(stage)

        val htmlEditor = HTMLEditor()
        htmlEditor.htmlText = "Hello <a href=\"https://github.com/TestFX/TestFX\">world</a>"

        println(htmlEditor.htmlText)
        // build the scene graph
        layout.top = topMenuView
        layout.center = htmlEditor
        layout.padding = Insets(5.0)

        // create and show the scene
        val scene = Scene(layout)

        stage.width = 800.0
        stage.height = 500.0
        stage.scene = scene
        stage.isResizable = true
        stage.title = "File Browser"
        stage.show()
    }
}
