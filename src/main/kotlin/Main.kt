
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.singleWindowApplication
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import java.awt.BorderLayout
import java.awt.Container
import javax.swing.JPanel
import javafx.scene.control.Button as JFXButton
import javafx.scene.paint.Color as JFXColor
import javafx.scene.text.Text as JFXText
import javafx.scene.web.HTMLEditor as JFXHTMLEditor

fun main() = singleWindowApplication{
    // JavaFX components
    val jfxpanel = remember { JFXPanel() }
    val jfxtext = remember { JFXText() }
//    val htmleditor = Platform.runLater{
//        remember { JFXHTMLEditor()}
//    }
    var htmlTextStr = rememberSaveable { mutableStateOf("Hello <b>World</b>. This <i><strike>text</strike>sentence</i> is form<b>att<u>ed</u></b> in simple html.")}
    // The current container (depending on how you are using the CFD,
    // this could be ComposeWindow or ComposePanel)
    val container = this.window // ComposeWindow

    val counter = remember { mutableStateOf(0) }
    val inc: () -> Unit = {
        counter.value++
        // update JavaFX text component
        Platform.runLater {
            jfxtext.text = "JAVAFX! ${htmlTextStr.value}"
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("COMPOSE: ${htmlTextStr.value}")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(top = 80.dp, bottom = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))
            // The "Box" is strictly necessary to properly sizing and positioning the JFXPanel container.
            Box(
                modifier = Modifier.height(200.dp).fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                ) {

                    JavaFXPanel(
                        root = container,
                        panel = jfxpanel,
                        // function to initialize JFXPanel, Group, Scene
                        onCreate = {
                            Platform.runLater {
                                val root = Group()
                                val scene = Scene(root, JFXColor.WHITESMOKE)
                                val htmleditor = JFXHTMLEditor()
                                val printHTMLButton = JFXButton("Produce HTML Code")
                                printHTMLButton.onMouseClicked = EventHandler {
                                    println(htmleditor.htmlText)
                                    htmlTextStr.value = htmleditor.htmlText
                                }
                                htmleditor.htmlText = htmlTextStr.value
                                root.children.addAll(htmleditor, printHTMLButton)
                                jfxpanel.scene = scene
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Button(text: String = "", action: (() -> Unit)? = null) {
    Button(
        modifier = Modifier.size(270.dp, 40.dp),
        onClick = { action?.invoke() }
    ) {
        Text(text)
    }
}

@Composable
public fun JavaFXPanel(
    root: Container,
    panel: JFXPanel,
    onCreate: () -> Unit
) {
    val container = remember { JPanel() }
    val density = LocalDensity.current.density

    Layout(
        content = {},
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size
            container.setBounds(
                (location.x / density).toInt(),
                (location.y / density).toInt(),
                (size.width / density).toInt(),
                (size.height / density).toInt()
            )
            container.validate()
            container.repaint()
        },
        measurePolicy = { _, _ ->
            layout(0, 0) {}
        }
    )

    DisposableEffect(Unit) {
        container.apply {
            setLayout(BorderLayout(0, 0))
            add(panel)

        }
        root.add(container)
        onCreate.invoke()
        onDispose {
            root.remove(container)
        }
    }
}