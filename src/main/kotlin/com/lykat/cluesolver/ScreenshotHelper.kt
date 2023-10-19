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

fun triggerPasteInWebView(webView: WebView, data: String) {
    javafx.application.Platform.runLater {
        //language=JavaScript
        webView.engine.executeScript(
            """
(() => {
    const pasteEvent = new Event('paste', {
        bubbles: true,
        cancelable: true
    });
    pasteEvent.clipboardData = {
        items: [ {
            type: 'image/png',
            kind: 'file',
            getAsFile: () => {
                const dataUri = '$data';
                const byteString = atob(dataUri.split(',')[1]);
                const mimeString = dataUri.split(',')[0].split(':')[1].split(';')[0];
                const buffer = new Uint8Array(byteString.length);
                for (let i = 0; i < byteString.length; i++) {
                    buffer[i] = byteString.charCodeAt(i);
                }
                return new Blob([buffer], {type: mimeString});
            }
        } ]
    }
    const pasteEventListener = document.querySelector(".forcehidden");
    if (pasteEventListener) pasteEventListener.onpaste(pasteEvent);
})();
"""
        )
    }
}

fun imageToDataURI(image: BufferedImage, format: String): String {
    val os = ByteArrayOutputStream()
    ImageIO.write(image, format, os)
    return "data:image/$format;base64," + Base64.getEncoder().encodeToString(os.toByteArray())
}