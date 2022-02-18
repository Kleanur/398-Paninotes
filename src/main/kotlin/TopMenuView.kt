import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.scene.web.HTMLEditor
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.jsoup.Jsoup
import java.io.File
import kotlin.system.exitProcess


class TopMenuView(val model: Model, val htmlEditor: HTMLEditor,val stage: Stage) : Pane(), IView{

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
        val fileOpen = createAddToMenu(fileMenu,"Open")
        val fileSave = createAddToMenu(fileMenu,"Save")
        val fileQuit = createAddToMenu(fileMenu,"Quit")
        menuBar.menus.add(fileMenu)

        // View: Home, Prev Next
        val viewMenu = Menu("View")
        val viewHome = createAddToMenu(viewMenu,"Home")
        val viewPrev = createAddToMenu(viewMenu,"Prev")
        val viewNext = createAddToMenu(viewMenu,"Next")
        menuBar.menus.add(viewMenu)

        // Action: Rename, Move, Delete
        val actionMenu = Menu("Action")
        val actionRename = createAddToMenu(actionMenu,"Rename")
        val actionMove = createAddToMenu(actionMenu,"Move")
        val actionDelete = createAddToMenu(actionMenu,"Delete")
        menuBar.menus.add(actionMenu)
        // Option:
        val optionMenu = Menu("Option")
        val optionSearch = createAddToMenu(optionMenu, "Search")
        menuBar.menus.add(optionMenu)

        fileMenu.id = "menu-fileMenu"
        fileNewNote.id = "menuitem-fileNewNote"
        fileOpen.id = "menuitem-fileOpen"
        fileSave.id = "menuitem-fileSave"
        fileQuit.id = "menuitem-fileQuit"
        viewMenu.id = "menu-viewMenu"
        viewHome.id = "menuitem-viewHome"
        viewPrev.id = "menuitem-viewPrev"
        viewNext.id = "menuitem-viewNext"
        actionMenu.id = "menu-actionMenu"
        actionDelete.id = "menuitem-actionDelete"
        actionRename.id = "menuitem-actionRename"
        actionMove.id = "menuitem-actionMove"
        optionMenu.id = "menu-optionMenu"

        fileNewNote.setOnAction {
            val directoryDialog = DirectoryChooser()
            directoryDialog.title = "Select an Notebook Folder"
            directoryDialog.initialDirectory = model.testNotebookDir
            val directory: File? = directoryDialog.showDialog(stage)
            //this the notebook
            model.setCurrentOpenFolder(directory)
            //create the note
            model.createHTMLFilePopup(directory)
        }

        fileOpen.setOnAction {
            val fileDialog = FileChooser()
            fileDialog.title = "Select an HTML File"
            val extFilter = FileChooser.ExtensionFilter("HTML files (*.html)", "*.html")
            fileDialog.extensionFilters.add(extFilter)
            val file: File? = fileDialog.showOpenDialog(stage)
            model.openAndReadHTMLFile(file)
        }

        fileSave.setOnAction {
            print(htmlEditor.htmlText)
            model.currentFile?.writeText(htmlEditor.htmlText)
        }


        fileQuit.setOnAction {
            exitProcess(0)
        }

        // Add a shortcut CTRL+Q for file->quit
        fileNewNote.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        //need new directory, open directory
        fileOpen.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
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

        this.children.add(menuBar)



        stage.setOnCloseRequest {
            if(model.currentFile != null) {
                val confirmationAlert = Alert(Alert.AlertType.CONFIRMATION)
                confirmationAlert.title = "Paninotes"
                confirmationAlert.contentText = "Save changes to ${model.currentFile?.name}?"
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
                        model.currentFile?.writeText(htmlEditor.htmlText)
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
        if(model.currentFile != null){
            htmlEditor.htmlText = model.currentFileContents
            htmlEditor.isVisible = true
        } else {
            //hide the editor maybe welcome message
            htmlEditor.isVisible = false
        }
    }
}
