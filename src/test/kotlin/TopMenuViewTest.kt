import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxAssert
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.matcher.control.LabeledMatchers

@ExtendWith(ApplicationExtension::class)
class TopMenuViewTest {

    @Start
    private fun start(stage: Stage) {
        val model = Model()
        val htmlEditor = HTMLEditor()
        val layout = BorderPane()
        val topMenuView = TopMenuView(model, htmlEditor, stage)

        layout.top = topMenuView

        stage.scene = Scene(layout)
        stage.show()
    }

    @Test
    fun first_test(robot: FxRobot) {
        FxAssert.verifyThat("#menubar-file", LabeledMatchers.hasText("File"))
    }
}