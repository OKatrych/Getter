package eu.warble.androidsftp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import eu.warble.getter.Getter
import eu.warble.getter.LoadDirectoryCallback
import eu.warble.getter.model.Credentials
import eu.warble.getter.model.GetterFile
import kotlinx.android.synthetic.main.activity_main.button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            loadDirectory()
            button.isEnabled = false
        }
    }

    private fun loadDirectory() {
        Getter.init("sftp.pjwstk.edu.pl", Credentials("LOGIN", "PASSWORD"),
                onSuccess = {
                    Getter.loadDirectory("/", object : LoadDirectoryCallback {
                        override fun onDirectoryLoaded(getterFiles: List<GetterFile>) {
                            println(getterFiles)
                        }

                        override fun onError(error: String) {
                            System.err.println(error)
                        }
                    })
                },
                onFailure = { Log.e("Getter: MainActivity", it.message) }
        )
    }
}