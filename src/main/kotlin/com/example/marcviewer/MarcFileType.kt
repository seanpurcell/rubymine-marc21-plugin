package com.example.marcviewer

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import javax.swing.Icon
import javax.swing.UIManager

object MarcFileType : LanguageFileType(MarcLanguage) {
    override fun getName() = "MARC"
    override fun getDescription() = "MARC21 binary record file"
    override fun getDefaultExtension() = "mrc"
    override fun getIcon(): Icon? = UIManager.getIcon("FileView.fileIcon")
}

object MarcLanguage : com.intellij.lang.Language("MARC")

class MarcFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(MarcFileType, MarcFileType.defaultExtension)
    }
}
