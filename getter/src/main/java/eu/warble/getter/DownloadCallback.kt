package eu.warble.getter

interface DownloadCallback{
    fun onProgressChanged(percent: Int)
    fun onFinish(destinationDirectory: String)
    fun onError()
}