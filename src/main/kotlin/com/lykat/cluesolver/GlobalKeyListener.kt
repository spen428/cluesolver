package com.lykat.cluesolver

import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import java.awt.Rectangle
import java.util.logging.Level
import java.util.logging.Logger

fun setupGlobalKeyListener(mainController: MainController) {
    val keyListener = object : NativeKeyListener {
        override fun nativeKeyReleased(event: NativeKeyEvent) {
        }

        override fun nativeKeyTyped(event: NativeKeyEvent) {
        }

        override fun nativeKeyPressed(event: NativeKeyEvent) {
            if (event.keyCode == 2 && event.modifiers == 8) handleAlt1()
            if (event.modifiers != 0) return
            when (event.keyCode) {
                57416 -> handleArrowKey("up")
                57424 -> handleArrowKey("down")
                57419 -> handleArrowKey("left")
                57421 -> handleArrowKey("right")
            }
        }

        private fun handleArrowKey(direction: String) {
            val firstInstruction = mainController.instructionList.firstOrNull()
            if (firstInstruction == null) return
            if (firstInstruction == direction) {
                mainController.instructionList.removeFirst()
            }
            if (firstInstruction != direction) {
                mainController.instructionList.add(0, getOpposite(direction))
            }
            mainController.updateInstructionsView()
        }

        private fun getOpposite(direction: String): String {
            return when (direction) {
                "up" -> "down"
                "down" -> "up"
                "left" -> "right"
                "right" -> "left"
                else -> throw IllegalArgumentException()
            }
        }

        private fun handleAlt1() {
            val screenshot = captureScreen(Rectangle(1920 + 520, 280, 950, 530))
            setImageToClipboard(screenshot)
            val data = imageToDataURI(screenshot, "png")
            triggerPasteInWebView(mainController.webView, data)
        }
    }

    try {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(keyListener)
        disableJNativeHookLogging()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun disableJNativeHookLogging() {
    val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
    logger.level = Level.OFF
    logger.useParentHandlers = false
}