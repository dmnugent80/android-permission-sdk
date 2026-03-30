package io.github.dmnugent80.androidpermissionsdk.api

/**
 * Logger interface for SDK internal logging.
 * Implement this to receive log messages from the SDK.
 */
fun interface PermissionSdkLogger {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)

    enum class LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}
