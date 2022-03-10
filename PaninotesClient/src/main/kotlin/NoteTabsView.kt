import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage

class NoteTabsView(val model: Model, val stage: Stage): TabPane(), IView {
    init {
        this.layoutView()
        this.id = "noteTabs"

        // Hacky thing so when the TabPane is not visible, it doesn't take up any empty space
        this.managedProperty().bind(this.visibleProperty())
    }

    private fun getNoteFromNotebookAndNoteName(notebookAndNoteName: String): Note? {
        val notebookAndNoteSplit: List<String> = notebookAndNoteName.split("/")
        val notebookTitle: String = notebookAndNoteSplit[0]
        val noteTitle: String = notebookAndNoteSplit[1]

        val notebook: Notebook? = model.getNotebookByTitle(notebookTitle)
        if (notebook != null) {
            return notebook.getNoteByTitle(noteTitle)
        }

        return null
    }

    private fun layoutView() {
        // If there are no open notes, then hide the TabPane
        this.isVisible = model.openNotes.size > 0

        model.openNotes.forEach { note ->
            // Check if we already have a tab for this open note
            // This string will be unique for each tab, since every notebook must have a different name
            val notebookAndNoteName: String = note.notebook!!.title + "/" + note.fileName!!

            if (this.tabs.filter {it.text == notebookAndNoteName}.size == 0) {
                val tab = Tab(notebookAndNoteName)

                tab.setOnSelectionChanged {
                    if (tab.isSelected) {
                        model.openNote(note)
                    }
                }

                tab.setOnClosed {
                    // Remove the note from the open notes in the model
                    val closedNote: Note? = getNoteFromNotebookAndNoteName(tab.text)
                    model.closeNote(closedNote)

                    // For some reason, tab.setOnSelectionChanged is run before tab.setOnClosed, so when model.openNote is called
                    // in setOnSelectionChanged, that will call layoutView again and re-add the tab we just closed
                    // So, make sure that the tab actually gets closed/removed

                    // Get the index of the tab that we need to close
                    var tabIndex: Int = -1

                    for (i in 0 until this.tabs.size) {
                        if (this.tabs[i].text == tab.text) {
                            tabIndex = i
                            break
                        }
                    }

                    if (tabIndex >= 0) this.tabs.removeAt(tabIndex)
                }

                this.tabs.add(tab)
                this.selectionModel.select(tab)
            }
        }
    }

    override fun update() {
        this.layoutView()
    }
}