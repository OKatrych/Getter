package eu.warble.getter.model

import com.jcraft.jsch.ChannelSftp
import net.schmizz.sshj.xfer.FilePermission
import java.io.File

data class GetterFile(var name: String, var path: String, var permissions: String,
                      var lsEntry: ChannelSftp.LsEntry){
    fun isDirectory() = lsEntry.attrs.isDir
}