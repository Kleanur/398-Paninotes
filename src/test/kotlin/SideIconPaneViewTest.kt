import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxAssert
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.matcher.base.NodeMatchers.*

@ExtendWith(ApplicationExtension::class)
class SideIconPaneViewTest {

    @Start
    private fun start(stage: Stage) {
        val model = Model()
        val sideNotebookPane = SideNotebookPaneView(model)
        val sideIconPane = SideIconPaneView(model, sideNotebookPane)
        val layout = BorderPane()
        val sidePane = HBox()
        sidePane.children.addAll(sideIconPane, sideNotebookPane)

        layout.left = sidePane

        stage.scene = Scene(layout)
        stage.show()
    }

    @Test
    fun notebookButtonShows(robot: FxRobot) {
        FxAssert.verifyThat("#sideIconPane-notebook-button", isVisible())
    }

    @Test
    fun searchButtonShows(robot: FxRobot) {
        FxAssert.verifyThat("#sideIconPane-search-button", isVisible())
    }

    @Test
    fun infoButtonShows(robot: FxRobot) {
        FxAssert.verifyThat("#sideIconPane-info-button", isVisible())
    }

    @Test
    fun openNotebookPane(robot: FxRobot) {
        // First, test that the notebook pane is closed on start
        FxAssert.verifyThat("#sideNotebookPane",  isInvisible())

        // After clicking on the notebook button, the notebook pane should now open
        robot.clickOn("#sideIconPane-notebook-button")
        FxAssert.verifyThat("#sideNotebookPane",  isVisible())

        // After clicking on the notebook button again, the notebook pane should close
        robot.clickOn("#sideIconPane-notebook-button")
        FxAssert.verifyThat("#sideNotebookPane",  isInvisible())
    }

    @Test
    fun openInfoDialog(robot: FxRobot) {
        // After clicking on the info button, an alert dialog should pop up
        robot.clickOn("#sideIconPane-info-button")
        FxAssert.verifyThat(".dialog-pane",  isVisible())

        // Press enter to close the info popup
        robot.press(KeyCode.ENTER).release(KeyCode.ENTER)
    }
}