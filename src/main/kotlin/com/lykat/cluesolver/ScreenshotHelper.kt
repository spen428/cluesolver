package com.lykat.cluesolver

import javafx.scene.web.WebView
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

fun setImageToClipboard(image: BufferedImage) {
    val transferable = object : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> {
            return arrayOf(DataFlavor.imageFlavor)
        }

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
            return DataFlavor.imageFlavor.equals(flavor)
        }

        @Throws(UnsupportedFlavorException::class)
        override fun getTransferData(flavor: DataFlavor?): Any {
            if (isDataFlavorSupported(flavor)) {
                return image
            }
            throw UnsupportedFlavorException(flavor)
        }
    }

    Toolkit.getDefaultToolkit().systemClipboard.setContents(transferable, null)
}

fun captureScreen(rect: Rectangle): BufferedImage {
    val robot = Robot()
    return robot.createScreenCapture(rect)
}

fun triggerPasteInWebView(webView: WebView) {
    javafx.application.Platform.runLater {
        webView.engine.executeScript("document.dispatchEvent(new Event('paste'));")
    }
}

fun imageToDataURI(image: BufferedImage, format: String): String {
    val os = ByteArrayOutputStream()
    ImageIO.write(image, format, os)
    return "data:image/$format;base64," + Base64.getEncoder().encodeToString(os.toByteArray())
}