package com.lykat.cluesolver

import javafx.scene.web.WebView
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.datatransfer.Transferable
import java.util.logging.Level
import java.util.logging.Logger

fun setupGlobalKeyListener(webView: WebView) {
    val keyListener = object : NativeKeyListener {
        override fun nativeKeyReleased(event: NativeKeyEvent) {
        }

        override fun nativeKeyTyped(event: NativeKeyEvent) {
        }

        override fun nativeKeyPressed(event: NativeKeyEvent) {
            if (event.keyCode == 2 && event.modifiers == 8) handleAlt1()
            if (event.modifiers != 0) return
            if (event.keyCode == 57416) handleUp()
            if (event.keyCode == 57424) handleDown()
            if (event.keyCode == 57419) handleLeft()
            if (event.keyCode == 57421) handleRight()
        }

        private fun handleRight() {
            println("handleRight")
        }

        private fun handleLeft() {
            println("handleLeft")
        }

        private fun handleDown() {
            println("handleDown")
        }

        private fun handleUp() {
            println("handleUp")
        }

        private fun handleAlt1() {
            println("handleAlt1")
            val screenshot = captureScreen(Rectangle(1920 + 520, 280, 950, 530))
            setImageToClipboard(screenshot)
            val data = imageToDataURI(screenshot, "png")
            triggerPasteInWebView(webView, data)
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