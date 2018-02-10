package eu.warble.getter

import eu.warble.getter.model.GetterFile

interface LoadDirectoryCallback {
    fun onDirectoryLoaded(getterFiles: List<GetterFile>)
    fun onError(error: String)
}