package com.lykat.cluesolver

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Main::class.java.getResource("main-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 550.0, 500.0)
        stage.title = "Clue Solver for RuneScape"
        stage.scene = scene
        stage.isAlwaysOnTop = true
        stage.maxWidth = 550.0
        stage.maxHeight = 500.0
        stage.x = 0.0
        stage.y = 0.0
        stage.opacity = 0.75
        stage.show()
    }
}

fun main() {
    Application.launch(Main::class.java)
}