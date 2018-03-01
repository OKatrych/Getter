package eu.warble.getter

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import eu.warble.getter.model.Credentials
import eu.warble.getter.model.GetterFile
import eu.warble.getter.utils.AppExecutors
import eu.warble.getter.utils.Converter
import eu.warble.getter.utils.FileManager
import java.io.File
import java.util.Vector
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


object Getter {
    private var serverURL: String
    private var credentials: Credentials
    private lateinit var session: Session
    private lateinit var channelSFTP: ChannelSftp
    private val downloadingQueue: BlockingQueue<GetterFile>
    var currentPath: String = ""

    init {
        downloadingQueue = LinkedBlockingQueue<GetterFile>()
        serverURL = "DEFAULT"
        credentials = Credentials("", "")
    }

    /**
     * Must to be called when init Getter
     */
    fun init(serverURL: String, credentials: Credentials,
             onSuccess: () -> Unit, onFailure: (exception: Throwable) -> Unit) {
        if (!isGetterInitialized(serverURL, credentials)) {
            this.serverURL = serverURL
            this.credentials = credentials
            AppExecutors.NETWORK().execute {
                try {
                    session = startNewSession()
                    channelSFTP = openSftpChannel(session)
                    AppExecutors.MAIN().execute(onSuccess)
                } catch (ex: Exception) {
                    AppExecutors.MAIN().execute { onFailure(ex) }
                }
            }
        } else {
            AppExecutors.MAIN().execute(onSuccess)
        }
    }

    private fun startNewSession(): Session {
        return JSch().getSession(credentials.login, serverURL, 22).apply {
            setPassword(credentials.password)
            setConfig("StrictHostKeyChecking", "no")
            connect(7000)
        }
    }

    private fun openSftpChannel(session: Session): ChannelSftp {
        return (session.openChannel("sftp") as ChannelSftp).apply {
            connect()
        }
    }

    fun disconnect() {
        try {
            if (session.isConnected)
                session.disconnect()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun download(getterFile: GetterFile, callback: DownloadCallback) {
        AppExecutors.NETWORK().execute {
            val downloadChannel = openSftpChannel(startNewSession())
            val newFile = FileManager.createFileInDownloadsFolder(getterFile.name)
            try {
                downloadChannel.get(getterFile.path, newFile.absolutePath, GetterProgressMonitor(callback))
                downloadChannel.exit()
                downloadChannel.session.disconnect()
            } catch (ex: SftpException) {
                AppExecutors.MAIN().execute(callback::onError)
            }
        }
    }

    fun loadDirectory(directory: GetterFile, callback: LoadDirectoryCallback) {
        if (!directory.isDirectory()) callback.onError("Given file is not a directory")
        AppExecutors.NETWORK().execute {
            try {
                if (!checkInMainDirectory(directory.path)) {
                    channelSFTP.cd(directory.path)
                    currentPath = channelSFTP.pwd()
                }
                val list = channelSFTP.ls(currentPath) as Vector<ChannelSftp.LsEntry>
                val directoryFiles: List<GetterFile> = list.map {
                    Converter.convertLsEntryToGetterFile(it, currentPath)
                }
                AppExecutors.MAIN().execute { callback.onDirectoryLoaded(directoryFiles) }
            } catch (exception: SftpException) {
                tryRestartSession(
                        onSuccess = { loadDirectory(directory, callback) },
                        onFailure = {
                            AppExecutors.MAIN().execute { callback.onError(it.toString()) }
                        }
                )
            }
        }
    }

    fun loadDirectory(path: String, callback: LoadDirectoryCallback) {
        if (!File(path).isDirectory) callback.onError("Given file is not a directory")
        AppExecutors.NETWORK().execute {
            try {
                if (!checkInMainDirectory(path)) {
                    channelSFTP.cd(path)
                    currentPath = channelSFTP.pwd()
                }
                val list = channelSFTP.ls(currentPath) as Vector<ChannelSftp.LsEntry>
                val directoryFiles: List<GetterFile> = list.map {
                    Converter.convertLsEntryToGetterFile(it, currentPath)
                }
                AppExecutors.MAIN().execute { callback.onDirectoryLoaded(directoryFiles) }
            } catch (exception: SftpException) {
                tryRestartSession(
                        onSuccess = { loadDirectory(path, callback) },
                        onFailure = {
                            AppExecutors.MAIN().execute { callback.onError(it.toString()) }
                        }
                )
            }
        }
    }

    //prevent to use ".." or "." when in main directory
    private fun checkInMainDirectory(path: String): Boolean {
        return "/" == currentPath && (".." == path || "." == path)
    }

    private fun tryRestartSession(onSuccess: () -> Unit, onFailure: (exception: Throwable) -> Unit) {
        try {
            if (session.isConnected) {
                session.disconnect()
                if (channelSFTP.isConnected)
                    channelSFTP.exit()
            }
            session = startNewSession()
            channelSFTP = openSftpChannel(session)
            onSuccess()
        } catch (ex: Exception) {
            onFailure(ex)
        }
    }

    private fun isGetterInitialized(serverURL: String, credentials: Credentials): Boolean {
        return this.serverURL == serverURL && this.credentials == credentials
    }
}