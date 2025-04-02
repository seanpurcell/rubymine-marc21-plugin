package com.example.marcviewer

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import org.marc4j.MarcStreamReader
import java.awt.BorderLayout
import java.io.FileInputStream
import javax.swing.*
import com.intellij.openapi.project.DumbAware


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
    private val textArea = JTextArea()

    init {
        textArea.text = parseMarcFile(file.path)
        textArea.isEditable = false
        panel.add(JScrollPane(textArea), BorderLayout.CENTER)
    }

    private fun parseMarcFile(path: String): String {
        val sb = StringBuilder()
        try {
            val reader = MarcStreamReader(FileInputStream(path))
            var count = 0
            while (reader.hasNext() && count < 25) {
                val record = reader.next()
                record.dataFields.forEach { field ->
                    sb.append("=${field.tag}  ")
                    field.subfields.forEach { sub ->
                        sb.append("$${sub.code}${sub.data} ")
                    }
                    sb.append("\n")
                }
                sb.append("\n")
                count++
            }
            if (reader.hasNext()) {
                sb.append("... (only showing first 25 records)\n")
            }
        } catch (e: Exception) {
            sb.append("Failed to parse MARC file: ${e.message}")
        }
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
    override fun getFile(): VirtualFile = file  // ✅ Add this line
    override fun dispose() {}
}

