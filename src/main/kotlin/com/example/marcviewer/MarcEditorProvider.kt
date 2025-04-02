package com.example.marcviewer

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import org.marc4j.MarcStreamReader
import java.awt.BorderLayout
import java.awt.Desktop
import java.io.FileInputStream
import java.net.URI
import javax.swing.*

class MarcEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.extension == "mrc"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return MarcEditor(file)
    }

    override fun getEditorTypeId(): String = "marc-viewer"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

class MarcEditor(private val file: VirtualFile) : UserDataHolderBase(), FileEditor {
    private val panel = JPanel(BorderLayout())
    private val editorPane = JEditorPane()

    init {
        editorPane.contentType = "text/html"
        editorPane.isEditable = false
        editorPane.text = parseMarcFile(file.path)

        // Make links clickable
        editorPane.addHyperlinkListener { e ->
            if (e.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop().browse(URI(e.url.toString()))
            }
        }

        panel.add(JScrollPane(editorPane), BorderLayout.CENTER)
    }

    private fun parseMarcFile(path: String): String {
        val sb = StringBuilder()
        sb.append("<html><body style='font-family: monospace; white-space: pre;'>")
        try {
            val reader = MarcStreamReader(FileInputStream(path))
            var count = 0
            while (reader.hasNext() && count < 25) {
                val record = reader.next()
                record.dataFields.forEach { field ->
                    sb.append("<span style='color:#1565c0; font-weight:bold;'>=${field.tag}</span>  ")
                    field.subfields.forEach { sub ->
                        val safeData = sub.data
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")

                        val content = if (safeData.startsWith("https://")) {
                            "<a href=\"$safeData\">$safeData</a>"
                        } else {
                            safeData
                        }
                        sb.append("<span style='color:#00acc1;'>\$${sub.code}</span>$content ")
                    }
                    sb.append("<br>")
                }
                sb.append("<br>")
                count++
            }
            if (reader.hasNext()) {
                sb.append("... (only showing first 25 records)<br>")
            }
        } catch (e: Exception) {
            sb.append("<span style='color:red;'>Failed to parse MARC file: ${e.message}</span>")
        }
        sb.append("</body></html>")
        return sb.toString()
    }

    override fun getComponent(): JComponent = panel
    override fun getPreferredFocusedComponent(): JComponent = panel
    override fun getName(): String = "MARC Viewer"
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = true
    override fun selectNotify() {}
    override fun deselectNotify() {}
    override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
    override fun getCurrentLocation(): FileEditorLocation? = null
    override fun getFile(): VirtualFile = file
    override fun dispose() {}
}
