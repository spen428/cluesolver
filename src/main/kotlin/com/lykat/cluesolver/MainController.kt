package com.lykat.cluesolver

import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.web.WebEvent
import javafx.scene.web.WebView
import java.util.*

class MainController {

    @FXML
    lateinit var webView: WebView

    @FXML
    lateinit var imageView: ImageView

    fun checkClipboardForImageAndUpdate(imageView: ImageView) {
        val clipboard = Clipboard.getSystemClipboard()
        if (clipboard.hasImage()) {
            imageView.image = clipboard.image
        }
    }

    fun initialize() {
        setupGlobalKeyListener(webView)
        setupClipboardListener(imageView)
        webView.engine.onAlert = EventHandler<WebEvent<String>> { event ->
            println(event.data)
        }
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
            }
        }
        webView.engine.load("https://runeapps.org/apps/clue/")
    }

    private fun setupClipboardListener(imageView: ImageView) {
        val timer = Timer(true)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Platform.runLater {
                    checkClipboardForImageAndUpdate(imageView)
                }
            }
        }, 0, 1000)
    }
}
