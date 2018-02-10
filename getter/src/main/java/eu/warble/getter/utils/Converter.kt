package eu.warble.getter.utils

import com.jcraft.jsch.ChannelSftp
import eu.warble.getter.model.FileType
import eu.warble.getter.model.GetterFile
import eu.warble.getter.model.Permission

object Converter{

    fun convertLsEntryToGetterFile(lsEntry: ChannelSftp.LsEntry, currentPath: String): GetterFile{
        return GetterFile(lsEntry.filename, currentPath + lsEntry.filename, lsEntry.attrs.permissionsString, lsEntry)
    }

    fun unixPermissionToNormal(permission: Int): Permission{
        TODO("not implemented")
    }
}