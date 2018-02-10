package eu.warble.getter

interface DownloadCallback{
    fun onProgressChanged(percent: Int)
    fun onEnd()
    fun onError()
}