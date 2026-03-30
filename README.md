# Android Permission SDK

Kotlin-first Android runtime permission SDK with a small public API and hidden implementation details.

## What v1 Supports

- Permission status inspection
- Suspend-based permission requests
- Rich permission outcomes:
  - `Denied(canRequestAgain)` for retry vs likely-permanent deny cases
  - Preflight outcomes for manifest/API-level support
  - In-progress outcomes for active request coordination
- Request-in-progress tracking (`isRequestInProgress(...)`)
- Education tracking (SharedPreferences)
- Configurable SDK logging (`PermissionSdkLogger`)
- Structured diagnostics event stream (`PermissionSdkEvent`)
- Optional diagnostics configuration (`PermissionSdkConfig`)
- Permissions:
  - `AppPermission.Camera`
  - `AppPermission.FineLocation`

## Requirements

- `minSdk = 24`
- `compileSdk = 36`
- Kotlin + coroutines
- Host app must declare required permissions in its `AndroidManifest.xml`

Example:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Install

Local multi-module usage:

```kotlin
dependencies {
    implementation(project(":permissionsdk"))
}
```

## Public API

```kotlin
interface AndroidPermissionSdk {
    fun getStatus(permission: AppPermission, activity: Activity): PermissionStatus
    fun shouldShowEducation(permission: AppPermission): Boolean
    fun markEducationShown(permission: AppPermission)
    fun isRequestInProgress(permission: AppPermission): Boolean
    suspend fun request(permission: AppPermission, activity: ComponentActivity): PermissionResult
}
```

Create an instance:

```kotlin
val sdk = AndroidPermissionSdkFactory.create(applicationContext)
```

Create an instance with observability hooks:

```kotlin
val config = PermissionSdkConfig.Builder()
    .logger { level, tag, message, throwable ->
        // Route SDK logs to your logging system.
    }
    .eventListener { event ->
        when (event) {
            is PermissionSdkEvent.LauncherRegistrationFailed -> {
                // Example: attach as crash breadcrumb/telemetry signal.
            }
            else -> Unit
        }
    }
    .debugMode(true)
    .build()

val sdk = AndroidPermissionSdkFactory.create(applicationContext, config)
```

## Observability

Use observability hooks when you want visibility into SDK request lifecycle behavior.

- `PermissionSdkConfig`:
  - `logger(...)`: optional log sink for SDK log messages
  - `eventListener(...)`: optional structured lifecycle callback
  - `debugMode(true)`: enables debug-level SDK logs
- `PermissionSdkLogger`:
  - Receives `log(level, tag, message, throwable)`
  - Log levels: `DEBUG`, `INFO`, `WARN`, `ERROR`
- `PermissionSdkEventListener`:
  - Receives `PermissionSdkEvent` callbacks for each request lifecycle stage
- `PermissionSdkEvent` variants:
  - `RequestStarted`: request orchestration has begun
  - `LauncherRegistrationFailed`: launcher registration failed (includes `IllegalStateException`)
  - `SystemResponseReceived`: raw system permission map received
  - `RequestCompleted`: final SDK result resolved (preflight short-circuit or request completion)

Typical uses:

- Analytics or request funnel telemetry
- Crash-report breadcrumbs for request failures
- Debug visibility into permission request internals

Behavior guarantees and caveats:

- Observability is optional. If no config is passed, SDK behavior remains unchanged.
- `debugMode(true)` controls debug log emission. Warning-level launcher-registration failure logs/events still emit when diagnostics are configured.
- Logger and event-listener callback failures are swallowed and do not alter permission flow outcomes.
- `RequestCompleted` can represent either preflight short-circuit outcomes or post-request resolution.

## Quick Usage

```kotlin
val status = sdk.getStatus(AppPermission.Camera, activity)

if (status is PermissionStatus.Denied && status.canRequestAgain) {
    // Optional: show rationale before requesting again.
}

if (sdk.shouldShowEducation(AppPermission.Camera)) {
    // Show your own rationale UI, then:
    sdk.markEducationShown(AppPermission.Camera)
}

if (sdk.isRequestInProgress(AppPermission.Camera)) {
    // Optional: disable request button while request is active.
}

when (val result = sdk.request(AppPermission.Camera, componentActivity)) {
    PermissionResult.Granted -> Unit
    is PermissionResult.Denied -> {
        if (result.canRequestAgain) {
            // Can retry with rationale flow.
        } else {
            // Likely permanent deny: guide user to Settings.
        }
    }
    PermissionResult.AlreadyInProgress -> Unit
    PermissionResult.MissingFromManifest -> Unit
    PermissionResult.UnavailableOnApiLevel -> Unit
    PermissionResult.Cancelled -> Unit
}
```

## Status and Result Semantics

`PermissionStatus` (current effective state):

- `Granted`: permission is currently granted
- `NotRequestedYet`: no request history for that permission
- `Denied(canRequestAgain)`:
  - `canRequestAgain = shouldShowRationale(activity, permission) || wasEverGranted(permission)`
  - This allows one-time-expiry/settings-revoke recovery to remain requestable when previously granted
- `MissingFromManifest`: required manifest declaration is missing
- `UnavailableOnApiLevel`: permission is not supported on current Android API level
- `RequestInProgress`: a request for this permission is currently active

`PermissionResult` (result of `request(...)`):

- `Granted`
- `Denied(canRequestAgain)`:
  - `canRequestAgain = shouldShowRationale(activity, permission)`
- `AlreadyInProgress`: request short-circuited because same permission request is active
- `MissingFromManifest`: request short-circuited due to manifest precondition failure
- `UnavailableOnApiLevel`: request short-circuited due to API-level support check
- `Cancelled`: request did not complete with a permission map

## Module Structure

```text
AndroidPermissionSDK/
  permissionsdk/                     // Android library module (AAR)
    src/main/java/io/github/dmnugent80/androidpermissionsdk/
      api/                           // Public SDK contract + facade + factory
      core/                          // Internal interfaces and business rules
      platform/                      // Android framework implementations
    src/test/                        // Unit tests (core logic)
    src/androidTest/                 // Instrumentation smoke tests
  sample/                            // Compose sample app
```

## Architecture

The SDK uses layered package architecture inside one publishable library module:

- `api` layer:
  - Public contract (`AndroidPermissionSdk`)
  - Public models (`AppPermission`, `PermissionStatus`, `PermissionResult`)
  - Public observability types (`PermissionSdkConfig`, `PermissionSdkLogger`, `PermissionSdkEvent`, `PermissionSdkEventListener`)
  - Wiring entry point (`AndroidPermissionSdkFactory`)
  - Facade implementation (`DefaultAndroidPermissionSdk`)
- `core` layer:
  - Internal abstractions: `PermissionChecker`, `PermissionEducationStore`, `PermissionGrantHistoryStore`, `PermissionRequestCoordinator`, `PermissionApiLevelChecker`, `PermissionManifestChecker`, `PermissionRationaleChecker`, `PermissionRequestTracker`
  - Business rules: `PermissionStatusResolver`, `PermissionResultResolver`
- `platform` layer:
  - Android adapters for permission checks and request coordination
  - SharedPreferences-backed stores for education and grant-history persistence
  - Concrete checkers/tracking: `AndroidPermissionApiLevelChecker`, `AndroidPermissionManifestChecker`, `AndroidPermissionRationaleChecker`, `InMemoryPermissionRequestTracker`

Dependency direction is one-way:

- `api` orchestrates `core` + `platform`
- `core` does not depend on Android UI components beyond `Activity` abstractions
- `platform` implements `core` interfaces
- `sample` depends on `permissionsdk` (never the reverse)

## Important Runtime Notes

- Request flow is backed by `ActivityResultContracts.RequestMultiplePermissions` via `ComponentActivity.activityResultRegistry`.
- `request(...)` works with `ComponentActivity` (including `FragmentActivity` subclasses).
- `request(...)` preflight short-circuits to:
  - `UnavailableOnApiLevel`
  - `MissingFromManifest`
  - `AlreadyInProgress`
- Observability is configured through `AndroidPermissionSdkFactory.create(context, config)` and consumed internally during request orchestration.
- `Cancelled` occurs when the request completes without a permission result map (for example, launcher setup failure or interrupted completion path).
- One-time grants are treated as `Granted` while active and `Denied` after expiration/revocation.

## Testing

Core unit tests cover SDK decision logic:

- `DefaultAndroidPermissionSdkTest`
- `PermissionStatusResolverTest`
- `PermissionResultResolverTest`

Sample unit tests cover:

- ViewModel state flows for refresh/request/education/debug-clear behavior
- Status explanation copy for `Denied` (including one-time-expiry/settings-revoke clarity)

Instrumentation smoke tests cover:

- SDK factory creation

## Build

```bash
./gradlew :permissionsdk:test :sample:test :permissionsdk:assembleRelease :sample:assembleDebug
```

## Publishing Prep

`permissionsdk` includes Maven Publish configuration:

- Group: `io.github.dmnugent80`
- Artifact: `androidpermissionsdk`
- Version: `0.1.0`

Local publish tasks:

- `publishReleasePublicationToMavenLocal`
- `publishToMavenLocal`
