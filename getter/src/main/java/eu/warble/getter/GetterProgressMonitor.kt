package eu.warble.getter

import com.jcraft.jsch.SftpProgressMonitor
import eu.warble.getter.utils.AppExecutors

class GetterProgressMonitor(private val callback: DownloadCallback) : SftpProgressMonitor {

    private var maxBytes = -1L
    private var currBytes = -1L
    private var lastProgress = 0
    private var destinationDirectory = ""

    /**
     * Will be called when a new operation starts.
     * @param op - a code indicating the direction of transfer, one of PUT and GET
     * @param dest - the destination file name.
     * @param max - the final count (i.e. length of file to transfer).
     */
    override fun init(op: Int, src: String, dest: String, max: Long) {
        maxBytes = max
        currBytes = max
        lastProgress = 0
        destinationDirectory = dest
    }

    /**
     * Will be called periodically as more data is transferred.
     * @param count - the number of bytes transferred so far
     * @return true if the transfer should go on, false if the transfer should be cancelled.
     */
    override fun count(count: Long): Boolean {
        currBytes -= count
        val percent = ((maxBytes - currBytes) * 100 / maxBytes).toInt()
        if (percent != lastProgress) {
            lastProgress = percent
            AppExecutors.MAIN().execute { callback.onProgressChanged(percent) }
        }
        return true
    }

    /**
     *  Will be called when the transfer ended, either because all the data was transferred,
     *  or because the transfer was cancelled.
     */
    override fun end() {
        AppExecutors.MAIN().execute({
            callback.onFinish(destinationDirectory)
        })
    }

}