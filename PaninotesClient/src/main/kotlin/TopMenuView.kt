import com.itextpdf.html2pdf.HtmlConverter
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import jfxtras.styles.jmetro.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


class TopMenuView(val model: Model, val htmlEditor: CustomHTMLEditor, val stage: Stage, val jMetro: JMetro) : Pane(),
    IView {

    private val LIGHT_STYLESHEET_URL = TopMenuView::class.java.getResource("css/light.css")?.toExternalForm()
    private val DARK_STYLESHEET_URL = TopMenuView::class.java.getResource("css/dark.css")?.toExternalForm()


    init {
        this.layoutView()
    }

    private fun layoutView() {
        val menuBar = MenuBar()

        //responsive menubar
        menuBar.prefWidthProperty().bind(stage.widthProperty())

        // File: Quit
        val fileMenu = Menu("File")
        val fileNewNote = createAddToMenu(fileMenu, "New Note")
        val fileSave = createAddToMenu(fileMenu, "Save")
        val fileQuit = createAddToMenu(fileMenu, "Quit")
        menuBar.menus.add(fileMenu)

        // Option:
        val optionMenu = Menu("Option")
        val optionSearch = createAddToMenu(optionMenu, "Search")
        val optionTheme = createAddToMenu(optionMenu, "Use Dark Theme")
        val optionRestoreBackup = createAddToMenu(optionMenu, "Restore Backup")
        val optionBackupCurrentNotebook = createAddToMenu(optionMenu, "Backup Current Notebook")
        val optionDeleteAllData = createAddToMenu(optionMenu, "Delete Backup Data")
        val optionUsage = createAddToMenu(optionMenu, "Usage Statistics")
        val optionExport = createAddToMenu(optionMenu, "Export To PDF")
        menuBar.menus.add(optionMenu)

        // Sort
        val sortMenu = Menu("Sort")
        val sortNotes = createAddToMenu(sortMenu, "Sort Notes (A-Z) <-> (Z-A)")
        val sortNoteBook = createAddToMenu(sortMenu, "Sort Notebook (A-Z) <-> (Z-A)")
        menuBar.menus.add(sortMenu)

        if (Config.darkTheme) optionTheme.text = "Use Light Theme"

        fileMenu.id = "menu-fileMenu"
        fileNewNote.id = "menuitem-fileNewNote"
        fileSave.id = "menuitem-fileSave"
        fileQuit.id = "menuitem-fileQuit"
        optionMenu.id = "menu-optionMenu"
        sortMenu.id = "menu-sortMenu"

        fileNewNote.setOnAction {
            // If there is currently a notebook open, then we will automatically create a new note in that notebook
            if (model.currentOpenNotebook != null) {
                model.createNotePopup(model.currentOpenNotebook!!)
            } else {
                // Get list of all notebook names
                val notebookNames: List<String> = model.notebooks.map { it.title }

                // If there is no notebooks, show an error popup telling the user to create a notebook first
                if (notebookNames.isEmpty()) {
                    val warningPopup = FlatAlert(AlertType.WARNING)
                    warningPopup.initOwner(stage)
                    warningPopup.headerText = "No Notebooks!"
                    warningPopup.contentText = "You have no notebooks! You can only create a note in a notebook"

                    warningPopup.showAndWait()
                } else {
                    // Open a choice dialog to prompt the user what notebook they want to create the note in
                    val chooseNotebookDialog: FlatChoiceDialog<String> =
                        FlatChoiceDialog(notebookNames[0], notebookNames)
                    chooseNotebookDialog.initOwner(stage)
                    chooseNotebookDialog.headerText = "Choose Notebook to create a note in:"

                    val result: Optional<String> = chooseNotebookDialog.showAndWait()

                    // If the result is present, that means the user pressed the OK button
                    // Otherwise, they pressed cancel, and we don't want to add the notebook
                    if (result.isPresent) {
                        // get the selected item
                        val selectedNotebookTitle: String = chooseNotebookDialog.selectedItem as String
                        if (selectedNotebookTitle.isNotEmpty()) {
                            val selectedNotebook: Notebook? = model.getNotebookByTitle(selectedNotebookTitle)
                            if (selectedNotebook != null) {
                                model.createNotePopup(selectedNotebook)
                            }
                        }
                    }
                }
            }
        }

        fileSave.setOnAction {
            model.saveNote(htmlEditor.htmlText)
        }


        fileQuit.setOnAction {
            StageUtils.saveOnClose(model, stage, htmlEditor)
        }

        // Add a shortcut CTRL+Q for file->quit
        fileNewNote.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        //need new directory, open directory
        fileSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        fileQuit.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
        optionSearch.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)


        optionSearch.setOnAction {
            val dialog = FlatTextInputDialog("")
            dialog.initOwner(stage)
            dialog.title = "Search"
            dialog.headerText = "Find Word"

            (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Search"

            val result = dialog.showAndWait()
            if (result.isPresent) {
                val entered = result.get()
                if (entered.compareTo("") == 0) {
                    (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "OK"
                    dialog.show()
                    dialog.headerText = "No Input"
                } else {
                    val noHtmlTags = Jsoup.parse(htmlEditor.htmlText).text()
                    println(htmlEditor.htmlText)
                    println(noHtmlTags)

                    val delim = " "
                    val list = noHtmlTags.split(delim)
                    val wordIndexes = ArrayList<Int>()

                    var outputString = ""
                    for ((i, item) in list.withIndex()) {
                        if (i != 0) {
                            outputString += " "
                        }
                        if ((item.lowercase()).compareTo(entered.lowercase()) == 0) {
                            outputString = "$outputString<mark>$item</mark>"
                            wordIndexes.add(i)
                        } else {
                            outputString += item
                        }
                    }

                    println(outputString)
                    val oldText = htmlEditor.htmlText
                    htmlEditor.htmlText = outputString

                    (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "OK"

                    dialog.headerText = "Found " + wordIndexes.size
                    dialog.showAndWait()
                    htmlEditor.htmlText = oldText
                }
            }
        }

        optionUsage.setOnAction {
            val usageInfo = FlatAlert(AlertType.CONFIRMATION)
            usageInfo.initOwner(stage)
            usageInfo.headerText = "Statistics:"
            usageInfo.title = "Usage Statistics"
            val noHtmlTags = Jsoup.parse(htmlEditor.htmlText).text()
            val delim = " "
            val list = noHtmlTags.split(delim)
            val textInParagraphs = Jsoup.parse(htmlEditor.htmlText).select("p")
            val emptyParagraphs = Jsoup.parse(htmlEditor.htmlText).select("p:empty")
            val paragraphs = textInParagraphs.size
            var characters = 0
            println(textInParagraphs)

            for (element in list) {
                for (j in element.indices) {
                    characters++
                }
            }

            println(emptyParagraphs.size)

            usageInfo.contentText = "Words: ${list.size}\n" +
                    "Characters (no spaces): ${characters}\n" +
                    "Characters (with spaces) ${characters + (noHtmlTags.length - characters - paragraphs)}\n" +
                    "Paragraphs: ${paragraphs}\n"

            //show the popup
            usageInfo.showAndWait()

        }

        optionTheme.setOnAction {
            val ss = jMetro.scene.stylesheets
            if (jMetro.style == Style.LIGHT) {
                ss.clear()
                jMetro.style = Style.DARK
                ss.add(DARK_STYLESHEET_URL)
                optionTheme.text = "Use Light theme"
            } else {
                ss.clear()
                jMetro.style = Style.LIGHT
                ss.add(LIGHT_STYLESHEET_URL)
                optionTheme.text = "Use Dark theme"
            }
        }

        optionRestoreBackup.setOnAction {
            model.restoreBackup()
        }


        optionBackupCurrentNotebook.setOnAction {
            model.makeBackup()
        }

        optionDeleteAllData.setOnAction {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/deleteAll"))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                println("Success ${response.statusCode()}")
                print(response.body().toString())
//                val noteList: List<Note> = mapper.readValue(response.body().toString())
//                print(noteList.size)
//                print(noteList.toString())
            } else {
                print("ERROR ${response.statusCode()}")
                print(response.body().toString())
            }
        }

        optionExport.setOnAction {

            //get current note
            if (model.currentNote != null) {
                val confirmationAlert = FlatAlert(AlertType.CONFIRMATION)
                confirmationAlert.initOwner(stage)
                confirmationAlert.contentText = "Export ${model.currentNote?.title} to PDF?"
                //show the popup
                val result = confirmationAlert.showAndWait()

                if (result.isPresent) {
                    println(result)
                    println(result.get())
                    if (result.get() == ButtonType.OK) {
                        println("Exporting note")
                        val htmlSource = model.currentNote!!.filePath!!
                        val directoryChooser = DirectoryChooser()
                        directoryChooser.initialDirectory = File(System.getProperty("user.dir"))
                        directoryChooser.title = "Choose where to export file on disk"

                        val exportDirectory = directoryChooser.showDialog(stage)
                        if (exportDirectory != null) {
                            val pdfDest = exportDirectory.resolve("${model.currentNote!!.title}.pdf")
                            HtmlConverter.convertToPdf(FileInputStream(htmlSource), FileOutputStream(pdfDest))
                        } else {
                            val alert = FlatAlert(AlertType.WARNING)
                            alert.headerText = "No folder selected"
                            alert.show()
                        }
                    }
                }
            } else {
                //TODO add status bar text
                val alert = FlatAlert(AlertType.WARNING)
                alert.headerText = "Please open a note first"
                alert.show()
            }


        }

        sortNoteBook.setOnAction {
            // sort alphabetically
            model.notebookReversed = !model.notebookReversed
            model.notifyViews()
        }

        sortNotes.setOnAction {
            model.notesReversed = !model.notesReversed
            model.notifyViews()
        }
        this.children.add(menuBar)
    }

    private fun createAddToMenu(menu: Menu, menuItemName: String): MenuItem {
        val menuItem = MenuItem(menuItemName)
        menu.items.add(menuItem)
        return menuItem
    }

    override fun update() {
        //add a condition to only show editor if there is file assigned to model.currentFile
        if (model.currentNote != null) {
            htmlEditor.htmlText = model.currentNote?.htmlText
            println("Html editor:${htmlEditor.htmlText}")
            htmlEditor.isVisible = true
        } else {
            //hide the editor maybe welcome message
            htmlEditor.isVisible = false
        }
    }
}
