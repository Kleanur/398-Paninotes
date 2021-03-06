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
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)
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

        //View
        val viewMenu = Menu("View")
        val viewTheme = createAddToMenu(viewMenu, "Use Dark Theme")
        menuBar.menus.add(viewMenu)

        //Sync
        val syncMenu = Menu("Sync")
        val syncRestoreBackup = createAddToMenu(syncMenu, "Restore Backup")
        val syncBackupCurrentNotebook = createAddToMenu(syncMenu, "Backup Current Notebook")
        val syncDeleteAllData = createAddToMenu(syncMenu, "Delete Backup Data")
        menuBar.menus.add(syncMenu)

        // Tools:
        val toolsMenu = Menu("Tools")
        val toolsSearch = createAddToMenu(toolsMenu, "Search")
        val toolsSandR = createAddToMenu(toolsMenu, "Search and Replace")
        val toolsUsage = createAddToMenu(toolsMenu, "Usage Statistics")
        val toolsExport = createAddToMenu(toolsMenu, "Export To PDF")
        menuBar.menus.add(toolsMenu)

        // Sort
        val sortMenu = Menu("Sort")
        val sortNote = createAddToMenu(sortMenu, "Toggle Note Sorting (A-Z)")
        val sortNoteBook = createAddToMenu(sortMenu, "Toggle Notebook Sorting (A-Z)")
        menuBar.menus.add(sortMenu)

        if (Config.darkTheme) viewTheme.text = "Use Light Theme"

        fileMenu.id = "menu-fileMenu"
        fileNewNote.id = "menuitem-fileNewNote"
        fileSave.id = "menuitem-fileSave"
        fileQuit.id = "menuitem-fileQuit"
        toolsMenu.id = "menu-toolsMenu"
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
        viewTheme.accelerator = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)
        syncBackupCurrentNotebook.accelerator = KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN)
        syncRestoreBackup.accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN)
        syncDeleteAllData.accelerator = KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)
        toolsSearch.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
        toolsSandR.accelerator = KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)
        toolsUsage.accelerator = KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)
        toolsExport.accelerator = KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)

        sortNoteBook.accelerator = KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN)
        sortNote.accelerator = KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN)

        toolsSandR.setOnAction {
            replaceText()
        }

        toolsSearch.setOnAction {
            searchText()
        }

        toolsUsage.setOnAction {
            usageStats()
        }

        viewTheme.setOnAction {
            val ss = jMetro.scene.stylesheets
            if (jMetro.style == Style.LIGHT) {
                ss.clear()
                jMetro.style = Style.DARK
                ss.add(DARK_STYLESHEET_URL)
                viewTheme.text = "Use Light theme"
            } else {
                ss.clear()
                jMetro.style = Style.LIGHT
                ss.add(LIGHT_STYLESHEET_URL)
                viewTheme.text = "Use Dark theme"
            }
        }

        syncRestoreBackup.setOnAction {
            model.restoreBackup()
        }


        syncBackupCurrentNotebook.setOnAction {
            model.makeBackup(model.currentOpenNotebook)
        }

        syncDeleteAllData.setOnAction {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/deleteAll"))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                logger.info("Success ${response.statusCode()}")
                logger.info(response.body().toString())
                generateAlertDialogPopup(
                    AlertType.ERROR, "Server is not running", "Please check if server is running"
                )
            } else {
                logger.info("ERROR ${response.statusCode()}")
                logger.info(response.body().toString())
                generateAlertDialogPopup(
                    AlertType.ERROR, "Server is not running", "Please check if server is running"
                )
            }
        }

        toolsExport.setOnAction {

            //get current note
            if (model.currentNote != null) {
                val confirmationAlert = FlatAlert(AlertType.CONFIRMATION)
                confirmationAlert.initOwner(stage)
                confirmationAlert.contentText = "Export ${model.currentNote?.title} to PDF?"
                //show the popup
                val result = confirmationAlert.showAndWait()

                if (result.isPresent) {
                    logger.info(result.toString())
                    logger.info(result.get().toString())
                    if (result.get() == ButtonType.OK) {
                        logger.info("Exporting note")
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

        sortNote.setOnAction {
            model.notesReversed = !model.notesReversed
            model.notifyViews()
        }

        this.children.add(menuBar)
    }

    private fun usageStats() {
        val usageInfo = FlatAlert(AlertType.CONFIRMATION)
        usageInfo.initOwner(stage)
        usageInfo.headerText = "Usage Statistics:"
        usageInfo.title = "Paninotes"
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

    private fun searchText() {
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
                var count = 0
                println(htmlEditor.htmlText)
                println(noHtmlTags)
                val text = replaceWord(htmlEditor.htmlText, entered, "<mark>$entered</mark>", true)

                val delim = " "
                val list = noHtmlTags.split(delim)

                var outputString = ""
                for (item in list) {
                    if (entered in item) {
                        count++
                    }
                }

                println(outputString)
                val oldText = htmlEditor.htmlText
                htmlEditor.htmlText = text
                (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "OK"
                dialog.headerText = "Found: $count"
                dialog.showAndWait()
                htmlEditor.htmlText = oldText
            }
        }
    }

    private fun createAddToMenu(menu: Menu, menuItemName: String): MenuItem {
        val menuItem = MenuItem(menuItemName)
        menu.items.add(menuItem)
        return menuItem
    }

    private fun replaceText() {
        val dialog = FlatTextInputDialog("")
        dialog.initOwner(stage)
        dialog.title = "Search and Replace"
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
                val oldText = htmlEditor.htmlText
                var count = 0
                val delim = " "
                val list = noHtmlTags.split(delim)
                val found = replaceWord(htmlEditor.htmlText, entered, "<mark>$entered</mark>", true)

                for (item in list) {
                    if (entered in item) {
                        count++
                    }
                }

                htmlEditor.htmlText = found
                (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Replace"
                dialog.headerText = "Replacing: $count"

                val result2 = dialog.showAndWait()
                val replacingWord = result2.get()
                val replacedStr = replaceWord(oldText, entered, replacingWord, false)
                htmlEditor.htmlText = replacedStr
            }
        }
    }

    private fun replaceWord(html: String, word: String, new: String, highlight: Boolean): String {
        var replaced: String
        val doc = Jsoup.parse(html) // document
        val els = doc.body().allElements

        for (e in els) {
            val tnList = e.textNodes()
            for (tn in tnList) {
                val orig = tn.text()
                tn.text(orig.replace(word, new))
            }
        }
        replaced = doc.toString()

        if (highlight) {
            replaced = replaced.replace("&lt;mark&gt;", "<mark>")
            replaced = replaced.replace("&lt;/mark&gt;", "</mark>")
        }

        return replaced
    }

    private fun generateAlertDialogPopup(type: Alert.AlertType, title: String, content: String) {
        val alertBox = FlatAlert(type)
        alertBox.initOwner(stage)
        alertBox.title = title
        val errorContent = Label(content)
        errorContent.isWrapText = true
        alertBox.dialogPane.content = errorContent
        alertBox.showAndWait()
    }

    override fun update() {
        //add a condition to only show editor if there is file assigned to model.currentFile
        if (model.currentNote != null) {
            htmlEditor.htmlText = model.currentNote?.htmlText
            logger.info("Html editor:${htmlEditor.htmlText}")
            htmlEditor.isVisible = true
        } else {
            //hide the editor maybe welcome message
            htmlEditor.isVisible = false
        }
    }
}
