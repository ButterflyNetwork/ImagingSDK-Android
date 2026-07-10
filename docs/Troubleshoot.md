# Troubleshooting

## Installation

In case any issues were encountered during installation of the SDK, please examine if it matches one of the known issues below.

## Operation

To help better understand how the SDK works, or to aid debugging, a host app can register a listener to receive logs from the SDK.

```kotlin
imaging.imagingLogListener = object : ImagingLogListener {
    override fun onLog(loggingLevel: LogLevel, tag: String, message: String) {
        Log.d(tag, "Butterfly SDK (level='$loggingLevel'): $message")
    }
}
```

## Known issues

### Missing credentials
```
Set BFLY_SDK_USER_NAME and BFLY_SDK_ACCESS_TOKEN environment variables to download Butterfly Imaging SDK.
```

The loader plugin could not find your credentials.

**Fix:** Ensure `BFLY_SDK_USER_NAME` and `BFLY_SDK_ACCESS_TOKEN` are set as environment variables on your machine, as described in the [Butterfly  Access Token](AccessToken.md) section. Restart Android Studio after setting them.

### Failed to download (HTTP 401 / 403)
```
Failed to download Butterfly Imaging SDK (HTTP 401 Unauthorized).
```

The username or access token used to authenticate the download is missing or incorrect.

**Fix:** Verify the `BFLY_SDK_USER_NAME` and `BFLY_SDK_ACCESS_TOKEN` environment variables exactly match the credentials provided during onboarding, with no extra whitespace or characters.

### Unknown host
```
java.net.UnknownHostException: sdk.butterflynetwork.com
```

The build machine cannot reach the SDK download server.

**Fix:** Ensure network connectivity is available, then rebuild.

### Checksum mismatch
```
Butterfly Imaging SDK checksum mismatch. Set up fails.
```

The downloaded SDK archive does not match the expected checksum, usually due to an interrupted or corrupted download.

**Fix:**

1. Delete the partially installed artifact from your local Maven repository:
   ```sh
   rm -rf $HOME/.m2/repository/com/butterflynetwork/
   ```
1. Rebuild your project to re-download the SDK.

### Could not resolve com.butterflynetwork.sdk:imaging
```
Could not find com.butterflynetwork.sdk:imaging:<version>.
```

Gradle cannot locate the SDK artifact installed by the loader plugin.

**Fix:** Ensure `mavenLocal()` is listed under `dependencyResolutionManagement { repositories { ... } }` in your `settings.gradle.kts`, then sync and rebuild.
