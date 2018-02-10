package eu.warble.getter.utils

import android.os.Environment
import eu.warble.getter.GetterException
import java.io.File

object FileManager {

    @Throws(GetterException::class)
    fun createFileInDownloadsFolder(name: String): File {
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), name)
        if (!file.exists() && !file.createNewFile())
            throw GetterException("Cannot create file")
        return file
    }

}