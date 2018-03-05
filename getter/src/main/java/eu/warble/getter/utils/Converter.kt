package eu.warble.getter.utils

import com.jcraft.jsch.ChannelSftp
import eu.warble.getter.model.GetterFile
import eu.warble.getter.model.Permission

object Converter {

    fun convertLsEntryToGetterFile(lsEntry: ChannelSftp.LsEntry, currentPath: String): GetterFile {
        val filePath = if (lsEntry.filename.startsWith("/"))
            currentPath + lsEntry.filename
        else
            "$currentPath/${lsEntry.filename}"
        return GetterFile(lsEntry.filename, filePath, lsEntry.attrs.permissionsString, lsEntry)
    }

    fun unixPermissionToNormal(permission: Int): Permission {
        TODO("not implemented")
    }
}