import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.stage.Stage
import kotlin.system.exitProcess

class TopMenuView(stage: Stage) : Pane() {
    private val stage: Stage?

    var fileMenu: Menu? = null
    var fileQuit: MenuItem? = null
    var viewMenu: Menu? = null
    var viewHome: MenuItem? = null
    var viewPrev: MenuItem? = null
    var viewNext: MenuItem? = null
    var actionMenu: Menu? = null
    var actionRename: MenuItem? = null
    var actionMove: MenuItem? = null
    var actionDelete: MenuItem? = null
    var optionMenu: Menu? = null
    var optionSearch: MenuItem? = null

    init {
        this.stage = stage

        this.layoutView()
        this.registerActions()
    }

    // TODO - add the actual menu items into here
    private fun layoutView() {
        val menuBar = MenuBar()

        // File: Quit
        fileMenu = Menu("File")
        fileQuit = MenuItem("Quit")
        fileMenu!!.items.add(fileQuit)
        menuBar.menus.add(fileMenu)

        // View: Home, Prev Next
        viewMenu = Menu("View")
        viewHome = MenuItem("Home")
        viewPrev = MenuItem("Prev")
        viewNext = MenuItem("Next")
        viewMenu!!.items.add(viewHome)
        viewMenu!!.items.add(viewPrev)
        viewMenu!!.items.add(viewNext)
        menuBar.menus.add(viewMenu)

        // Action: Rename, Move, Delete
        actionMenu = Menu("Action")
        actionRename = MenuItem("Rename")
        actionMove = MenuItem("Move")
        actionDelete = MenuItem("Delete")
        actionMenu!!.items.add(actionRename)
        actionMenu!!.items.add(actionMove)
        actionMenu!!.items.add(actionDelete)
        menuBar.menus.add(actionMenu)

        // Option: Search
        optionMenu = Menu("Option")
        menuBar.menus.add(optionMenu)
        optionSearch = MenuItem("Search")
        optionMenu!!.items.add(optionSearch)
        this.children.add(menuBar)
    }

    private fun registerActions() {
        fileQuit!!.setOnAction {
            exitProcess(0)
        }

        optionSearch!!.setOnAction {
            exitProcess(0)
        }

        // Add a shortcut CTRL+Q for file->quit
        fileQuit!!.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)

        // Add a shortcut CTRL+F for option->search
        optionSearch!!.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
    }
}