# Android Permission SDK

Kotlin-first Android runtime permission SDK with a small public API and hidden implementation details.

## What v1 Supports

- Permission status inspection
- Suspend-based permission requests
- Education tracking (SharedPreferences)
- Likely permanent-denial detection
- Open-app-settings helper
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
    suspend fun request(permission: AppPermission, activity: ComponentActivity): PermissionResult
    fun openAppSettings(activity: Activity)
}
```

Create an instance:

```kotlin
val sdk = AndroidPermissionSdkFactory.create(applicationContext)
```

## Quick Usage

```kotlin
val status = sdk.getStatus(AppPermission.Camera, activity)

if (sdk.shouldShowEducation(AppPermission.Camera)) {
    // Show your own rationale UI, then:
    sdk.markEducationShown(AppPermission.Camera)
}

val result = sdk.request(AppPermission.Camera, componentActivity)
if (result == PermissionResult.PermanentlyDenied) {
    sdk.openAppSettings(activity)
}
```

## Status and Result Semantics

`PermissionStatus` (current effective state):

- `Granted`: permission is currently granted
- `NotRequestedYet`: denied and no education/request history
- `Denied`: denied, history exists, and rationale is still true
- `PermanentlyDenied`: denied, history exists, and rationale is false

`PermissionResult` (result of `request(...)`):

- `Granted`
- `Denied`
- `PermanentlyDenied`
- `Cancelled` (request did not complete with a permission map)

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
  - Wiring entry point (`AndroidPermissionSdkFactory`)
  - Facade implementation (`DefaultAndroidPermissionSdk`)
- `core` layer:
  - Internal abstractions: `PermissionChecker`, `RationaleChecker`, `PermissionEducationStore`, `PermissionRequestCoordinator`, `AppSettingsOpener`
  - Business rules: `PermissionStatusResolver`, `PermissionResultResolver`, `PermanentDenialPolicy`
- `platform` layer:
  - Android adapters for permission checks, rationale checks, SharedPreferences persistence, settings intents, and request coordination

Dependency direction is one-way:

- `api` orchestrates `core` + `platform`
- `core` does not depend on Android UI components beyond `Activity` abstractions
- `platform` implements `core` interfaces
- `sample` depends on `permissionsdk` (never the reverse)

## Important Runtime Notes

- Request flow is backed by `ActivityResultContracts.RequestMultiplePermissions` via `ComponentActivity.activityResultRegistry`.
- `request(...)` works with `ComponentActivity` (including `FragmentActivity` subclasses).
- `Cancelled` can occur when the launcher cannot be registered or the request cannot be completed.

## Testing

Unit tests cover core decision logic:

- `PermanentDenialPolicyTest`
- `PermissionStatusResolverTest`
- `PermissionResultResolverTest`

Instrumentation smoke tests cover:

- SDK factory creation
- App settings intent generation

## Build

```bash
./gradlew :permissionsdk:test :permissionsdk:assembleRelease :sample:assembleDebug
```

## Publishing Prep

`permissionsdk` includes Maven Publish configuration:

- Group: `io.github.dmnugent80`
- Artifact: `androidpermissionsdk`
- Version: `0.1.0`

Local publish tasks:

- `publishReleasePublicationToMavenLocal`
- `publishToMavenLocal`
