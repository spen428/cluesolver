package com.lykat.cluesolver

import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.text.Text
import javafx.scene.web.WebEvent
import javafx.scene.web.WebView
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.collections.ArrayDeque

class MainController {

    var instructions: String = ""
    var instructionList: ArrayDeque<String> = ArrayDeque()

    @FXML
    lateinit var webView: WebView

    @FXML
    lateinit var sliderPuzzleInstructionsView: Text

    @FXML
    lateinit var coordinateView: WebView

    fun initialize() {
        setupGlobalKeyListener(this)
        setupClipboardListener()
        webView.engine.onAlert = EventHandler<WebEvent<String>> { event ->
            if (event.data.startsWith("LOG: ") && event.data.contains("degrees")) {
                updateCoordinateView(event.data.substring(5))
            }
        }
        webView.engine.load("http://localhost:8811")
        webView.engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                webView.engine.executeScript(
                    """
            console.log = function(message) {
                window.alert('LOG: ' + message);
            };
            console.warn = function(message) {
                window.alert('WARN: ' + message);
            };
            console.error = function(message) {
                window.alert('ERROR: ' + message);
            };
        """
                )
                val mapSettings =
                    "{\"jewelry\":{\"glory\":\"pota-red-0\",\"cbbrace\":\"pota-red-2\",\"digpendant\":\"pota-purple-2\",\"enlightened\":\"pota-red-4\",\"games\":\"pota-red-1\",\"duel\":\"pota-purple-0\",\"respawn\":\"pota-purple-1\",\"skneck\":\"pota-red-3\",\"travellers\":\"pota-red-5\"},\"fairyrings\":[\"BKP\",\"DIS\",\"AJR\",\"ALQ\",\"AKS\",\"ALP\",\"CKS\",\"CJS\",\"CKR\",\"\"],\"toggles\":{\"varrock\":\"default\",\"yanille\":\"yanille\",\"camelot\":\"default\",\"sent\":\"none\",\"arch\":\"none\"},\"hideTeleports\":false,\"mapmode\":\"3d\",\"extmenu\":true,\"enablePota\":true,\"enableFairy\":true}"
                webView.engine.executeScript("localStorage.setItem('map_settings', '$mapSettings')")
            }
        }

        coordinateView.engine.loadContent("<html><body><p>Coordinate clue solutions will appear here</p></body></html>")
    }

    private fun updateCoordinateView(data: String) {
        val regex = Regex("(\\d+) degrees (\\d+) minutes (north|south) (\\d+) degrees (\\d+) minutes (east|west)")
        val matchEntire = regex.matchEntire(data) ?: return
        val values = matchEntire.groupValues
        coordinateView.engine.executeScript(
            """
(() => {
    var xhr = new XMLHttpRequest();
    var url = "https://runescape.wiki/api.php";
    
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    
    xhr.onreadystatechange = () => {
        console.log(xhr.status);
        if (xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
            var jsonResponse = JSON.parse(xhr.responseText);
            if (jsonResponse && jsonResponse.parse && jsonResponse.parse.text) {
                const html = jsonResponse.parse.text["*"]
                    .replace('href="/', 'href="https://runescape.wiki/')
                    .replace('src="/', 'src="https://runescape.wiki/')
                    .replace('srcset="/', 'srcset="https://runescape.wiki/')
                document.body.innerHTML = html;
            }
        }
    };
    
    var text = "&text=%7B%7BCoordinate%7Cdegree1%3D${values[1]}%7Cminute1%3D${values[2]}%7Cdirection1%3D${values[3]}%7Cdegree2%3D${values[4]}%7Cminute2%3D${values[5]}%7Cdirection2%3D${values[6]}%7D%7D";
    var data = "action=parse&prop=text%7Climitreportdata&title=Calculator%3ATreasure_Trails%2FGuide%2FLocate&disablelimitreport=true&contentmodel=wikitext&format=json" + text;
    xhr.send(data);
})();
"""
        )
    }

    private fun setupClipboardListener() {
        val timer = Timer(true)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Platform.runLater {
                    checkForSliderInstructions(webView)
                }
            }
        }, 0, 1000)
    }

    private fun checkForSliderInstructions(webView: WebView) {
        //language=JavaScript
        val instructions = webView.engine.executeScript(
            """
(() => {
    const guideButton = document.querySelector("#slidestartbutton");
    if (!guideButton?.offsetParent) return;
    if (guideButton.textContent === "Guide") guideButton.click();
    return [...document.querySelectorAll('.listoutputstep')]
        .map(x => x.textContent)
        .filter(x => ['left', 'down', 'right', 'up'].includes(x));
})();
"""
        ).toString()

        if (instructions == "undefined") {
            return
        }

        if (instructions != this.instructions) {
            println(instructions)
            this.instructions = instructions
            this.instructionList = ArrayDeque(instructions.split(","))
            this.updateInstructionsView()
        }
    }

    fun updateInstructionsView() {
        val subList = if (this.instructionList.size >= 14) this.instructionList.subList(0, 14) else this.instructionList
        this.sliderPuzzleInstructionsView.text = subList.joinToString(" ") {
            when (it) {
                "up" -> "↑"
                "down" -> "↓"
                "left" -> "←"
                "right" -> "→"
                else -> it
            }
        }
    }
}
