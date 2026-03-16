package io.github.dmnugent80.androidpermissionsdk.internal

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

internal class PermissionRequestFragment : Fragment() {
    private var pendingCallback: ((Map<String, Boolean>) -> Unit)? = null
    private lateinit var requestLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val callback = pendingCallback
            pendingCallback = null
            callback?.invoke(result)
        }
    }

    fun requestPermissions(
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        if (pendingCallback != null) {
            onResult(emptyMap())
            return
        }

        pendingCallback = onResult
        requestLauncher.launch(permissions)
    }

    fun clearPendingRequest() {
        pendingCallback = null
    }
}
