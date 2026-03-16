# Android Permission SDK

Kotlin-first Android runtime permission SDK with a minimal public API and hidden platform internals.

## Modules

- `:permissionsdk` - publishable Android library module (AAR)
- `:sample` - Compose sample app demonstrating SDK usage

## Features (v1)

- Permission status inspection
- Suspend-based permission request API
- Education tracking (SharedPreferences)
- Likely permanent denial detection
- Open app settings helper
- Supported permissions:
  - `Camera`
  - `FineLocation`

## Install (local module)

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

Create the SDK:

```kotlin
val sdk = AndroidPermissionSdkFactory.create(applicationContext)
```

## Usage

```kotlin
val status = sdk.getStatus(AppPermission.Camera, activity)
if (sdk.shouldShowEducation(AppPermission.Camera)) {
    sdk.markEducationShown(AppPermission.Camera)
}

val result = sdk.request(AppPermission.Camera, componentActivity)
if (result == PermissionResult.PermanentlyDenied) {
    sdk.openAppSettings(activity)
}
```

## Build

```bash
./gradlew :permissionsdk:test :permissionsdk:assembleRelease :sample:assembleDebug
```

## Publishing prep

`permissionsdk` includes Maven Publish configuration and supports:

- `publishReleasePublicationToMavenLocal`
- `publishToMavenLocal`

