package com.lykat.cluesolver

import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.text.Text
import javafx.scene.web.WebEvent
import javafx.scene.web.WebView
import java.util.*
import kotlin.collections.ArrayDeque

class MainController {

    var instructions: String = ""
    var instructionList: ArrayDeque<String> = ArrayDeque()

    @FXML
    lateinit var webView: WebView

    @FXML
    lateinit var sliderPuzzleInstructionsView: Text

    fun initialize() {
        setupGlobalKeyListener(this)
        setupClipboardListener()
        webView.engine.onAlert = EventHandler<WebEvent<String>> { event -> println(event.data) }
        webView.engine.load("http://localhost:8811")
        webView.engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                setupAlertForConsoleOutput()
                setupLocalStorage()
            }
        }
    }

    private fun setupLocalStorage() {
        val mapSettings =
            "{\"jewelry\":{\"glory\":\"pota-red-0\",\"cbbrace\":\"pota-red-2\",\"digpendant\":\"pota-purple-2\",\"enlightened\":\"pota-red-4\",\"games\":\"pota-red-1\",\"duel\":\"pota-purple-0\",\"respawn\":\"pota-purple-1\",\"skneck\":\"pota-red-3\",\"travellers\":\"pota-red-5\"},\"fairyrings\":[\"BKP\",\"DIS\",\"AJR\",\"ALQ\",\"AKS\",\"ALP\",\"CKS\",\"CJS\",\"CKR\",\"\"],\"toggles\":{\"varrock\":\"default\",\"yanille\":\"yanille\",\"camelot\":\"default\",\"sent\":\"none\",\"arch\":\"none\"},\"hideTeleports\":false,\"mapmode\":\"3d\",\"extmenu\":true,\"enablePota\":true,\"enableFairy\":true}"
        webView.engine.executeScript("localStorage.setItem('map_settings', '$mapSettings')")
    }

    private fun setupAlertForConsoleOutput() {
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
