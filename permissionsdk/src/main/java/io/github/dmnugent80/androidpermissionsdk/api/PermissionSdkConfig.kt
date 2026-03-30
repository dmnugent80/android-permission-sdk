package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Configuration options for the permission SDK.
 * Use [Builder] to construct instances.
 */
class PermissionSdkConfig private constructor(
    val logger: PermissionSdkLogger?,
    val eventListener: PermissionSdkEventListener?,
    val isDebugMode: Boolean
) {
    class Builder {
        private var logger: PermissionSdkLogger? = null
        private var eventListener: PermissionSdkEventListener? = null
        private var debugMode: Boolean = false

        fun logger(logger: PermissionSdkLogger?) = apply { this.logger = logger }
        fun eventListener(listener: PermissionSdkEventListener?) = apply { this.eventListener = listener }
        fun debugMode(enabled: Boolean) = apply { this.debugMode = enabled }

        fun build(): PermissionSdkConfig = PermissionSdkConfig(
            logger = logger,
            eventListener = eventListener,
            isDebugMode = debugMode
        )
    }

    companion object {
        val DEFAULT = PermissionSdkConfig(
            logger = null,
            eventListener = null,
            isDebugMode = false
        )
    }
}
