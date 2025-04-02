package com.example.marcviewer

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import org.marc4j.MarcStreamReader
import java.awt.BorderLayout
import java.io.FileInputStream
import javax.swing.*
import javax.swing.event.HyperlinkEvent

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
        editorPane.text = parseMarcFile(file.path)
        editorPane.isEditable = false
        editorPane.addHyperlinkListener {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    java.awt.Desktop.getDesktop().browse(it.url.toURI())
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(panel, "Failed to open link: ${e.message}")
                }
            }
        }
        panel.add(JScrollPane(editorPane), BorderLayout.CENTER)
    }

    private fun parseMarcFile(path: String): String {
        val sb = StringBuilder()
        sb.append("<html><body style='font-family: monospace; background-color: #f4f4f4;'>")
        try {
            val reader = MarcStreamReader(FileInputStream(path))
            var count = 0
            while (reader.hasNext() && count < 25) {
                val record = reader.next()
                record.dataFields.forEach { field ->
                    val indicators = ("${field.indicator1}${field.indicator2}").padEnd(2, ' ')
                    sb.append("<span style='color:blue'>=${field.tag}</span> ")
                    sb.append("<span style='color:#555'>$indicators</span> ")
                    field.subfields.forEach { sub ->
                        val data = sub.data.replace("<", "&lt;").replace(">", "&gt;")
                        val formatted = if (data.startsWith("http://") || data.startsWith("https://")) {
                            "<a href='${data}'>\$${sub.code}${data}</a>"
                        } else {
                            "<span style='color:#333'>\$${sub.code}</span><span style='color:#000'>${data}</span>"
                        }
                        sb.append("$formatted ")
                    }
                    sb.append("<br>")
                }
                sb.append("<br>")
                count++
            }
            if (reader.hasNext()) {
                sb.append("<i>... (only showing first 25 records)</i><br>")
            }
        } catch (e: Exception) {
            sb.append("<span style='color:red'>Failed to parse MARC file: ${e.message}</span>")
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
