import io.github.palexdev.materialfx.controls.MFXButton
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color

class SideIconPaneView(val model: Model, val sideNotebookPaneView: SideNotebookPaneView): GridPane(), IView {

    private val notebookButton = MFXButton()
    private val searchButton = MFXButton()
    private val infoButton = MFXButton()
    init {
        this.layoutView()
        this.border = Border(
            BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT
            )
        )
        this.add(notebookButton, 0, 0)
        this.add(searchButton, 0, 1)
        this.add(infoButton, 0, 2)
        
        // Default don't show the notebook pane
        sideNotebookPaneView.isVisible = false
    }

    private fun layoutView() {
        notebookButton.id = "sideIconPane-notebook-button"
        searchButton.id = "sideIconPane-search-button"
        infoButton.id = "sideIconPane-info-button"

        // Set up the images and buttons for the sidebar
        val notebookImage = Image("notebook_icon.png")
        val notebookImageView = ImageView(notebookImage)
        notebookImageView.isPreserveRatio = true
        notebookImageView.fitHeight = 20.0

        val searchImage = Image("search_icon.png")
        val searchImageView = ImageView(searchImage)
        searchImageView.isPreserveRatio = true
        searchImageView.fitHeight = 20.0

        val infoImage = Image("info_icon.png")
        val infoImageView = ImageView(infoImage)
        infoImageView.isPreserveRatio = true
        infoImageView.fitHeight = 20.0

        notebookButton.setPrefSize(20.0, 20.0)
        searchButton.setPrefSize(20.0, 20.0)
        infoButton.setPrefSize(20.0, 20.0)

        notebookButton.graphic = notebookImageView
        searchButton.graphic = searchImageView
        infoButton.graphic = infoImageView


        this.vgap = 3.0
        this.padding = Insets(5.0)

        // Button Actions
        notebookButton.setOnAction {
            // Toggle the side notebook pane view
            sideNotebookPaneView.isVisible = !sideNotebookPaneView.isVisible
        }

        searchButton.setOnAction {
        }

        infoButton.setOnAction {
            val popup = Alert(Alert.AlertType.INFORMATION)
            popup.title = "Note HTML Metadata Info"
            popup.dialogPane.content =  Label(model.currentNote?.fileMetadata.toString())
            popup.show()
        }
    }

    override fun update() {
        this.layoutView() //TODO don't want to refresh everything

        //add a condition to only show editor if there is file assigned to model.currentFile
        infoButton.isVisible = model.currentNote != null
    }
}