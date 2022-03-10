
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess

class TopMenuView(val model: Model, val htmlEditor: HTMLEditor,val stage: Stage) : Pane(), IView{
    val mapper = jacksonObjectMapper()

    init {
        this.layoutView()
    }

    // TODO - add the actual menu items into here
    private fun layoutView() {
        val menuBar = MenuBar()

        //responsive menubar
        menuBar.prefWidthProperty().bind(stage.widthProperty())

        // File: Quit
        val fileMenu = Menu("File")
        val fileNewNote = createAddToMenu(fileMenu,"New Note")
        val fileSave = createAddToMenu(fileMenu,"Save")
        val fileQuit = createAddToMenu(fileMenu,"Quit")
        menuBar.menus.add(fileMenu)

        // Option:
        val optionMenu = Menu("Option")
        val optionSearch = createAddToMenu(optionMenu, "Search")
        val optionTestHTTP = createAddToMenu(optionMenu,"HTTPTEST")
        menuBar.menus.add(optionMenu)

        fileMenu.id = "menu-fileMenu"
        fileNewNote.id = "menuitem-fileNewNote"
        fileSave.id = "menuitem-fileSave"
        fileQuit.id = "menuitem-fileQuit"
        optionMenu.id = "menu-optionMenu"

        fileNewNote.setOnAction {
            // If there is currently a notebook open, then we will automatically create a new note in that notebook
            if (model.currentOpenNotebook != null) {
                model.createNotePopup(model.currentOpenNotebook!!)
            } else {
                // Get list of all notebook names
                val notebookNames: List<String> = model.notebooks.map({ it.title })

                // Open a choice dialog to prompt the user what notebook they want to create the note in
                val chooseNotebookDialog: ChoiceDialog<String> = ChoiceDialog(notebookNames[0], notebookNames)
                chooseNotebookDialog.title = "Paninotes"
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
                            model.createNotePopup(selectedNotebook!!)
                        }
                    }
                }
            }
        }

        fileSave.setOnAction {
            print(htmlEditor.htmlText)
            model.currentNote?.saveNote(htmlEditor.htmlText)
        }


        fileQuit.setOnAction {
            exitProcess(0)
        }

        // Add a shortcut CTRL+Q for file->quit
        fileNewNote.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        //need new directory, open directory
        fileSave.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        fileQuit.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)


        optionSearch.setOnAction{
            val dialog = TextInputDialog("")
            dialog.title = "Search"
            dialog.headerText = "Find Word"

            (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Search"

            val oldText = htmlEditor.htmlText
            val result = dialog.showAndWait()
            if (result.isPresent) {
                val entered = result.get()
                if (entered.compareTo("") == 0) {
                    (dialog.dialogPane.lookupButton(ButtonType.OK) as Button).text = "OK"
                    dialog.show()
                    dialog.headerText = "No Input"
                } else {
                    val noHtmlTags = Jsoup.parse(htmlEditor.htmlText).text()
                    var inputtedText = htmlEditor.htmlText
                    println(htmlEditor.htmlText)
                    println(noHtmlTags)

                    val delim = " "
                    val list = noHtmlTags.split(delim)
                    var wordIndexes = ArrayList<Int>()
                    var outputString = ""
                    for ((i, item) in list.withIndex()) {
                        if (i != 0) {
                            outputString += " "
                        }
                        if((item.lowercase()).compareTo(entered.lowercase()) == 0 ) {
                            outputString = outputString + "<mark>" + item + "</mark>"
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

        optionTestHTTP.setOnAction {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080"))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if(response.statusCode() == 200){
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
        this.children.add(menuBar)



        stage.setOnCloseRequest {
            if(model.currentNote != null) {
                val confirmationAlert = Alert(Alert.AlertType.CONFIRMATION)
                confirmationAlert.title = "Paninotes"
                confirmationAlert.contentText = "Save changes to ${model.currentNote?.fileName}?"
                confirmationAlert.buttonTypes.clear()
                val discardButton = ButtonType("Discard")
                val saveButton = ButtonType("Save")
                val cancelButton = ButtonType("Cancel")
                confirmationAlert.buttonTypes.addAll(discardButton, saveButton, cancelButton)
                //show the popup
                val result = confirmationAlert.showAndWait()

                if (result.isPresent) {
                    println(result)
                    println(result.get())
                    if (result.get() == saveButton) {
                        print(htmlEditor.htmlText)
                        model.currentNote?.saveNote(htmlEditor.htmlText)
                        Platform.exit()
                        exitProcess(0)
                    } else if (result.get() == cancelButton) {
                        it.consume()
                    }
                }
            }
        }
    }

    private fun createAddToMenu(menu: Menu, menuItemName:String): MenuItem {
        val menuItem = MenuItem(menuItemName)
        menu.items.add(menuItem)
        return menuItem
    }

    override fun update() {
        //add a condition to only show editor if there is file assigned to model.currentFile
        if(model.currentNote != null){
            htmlEditor.htmlText = model.currentNote?.fileContents
            htmlEditor.isVisible = true
        } else {
            //hide the editor maybe welcome message
            htmlEditor.isVisible = false
        }
    }
}
