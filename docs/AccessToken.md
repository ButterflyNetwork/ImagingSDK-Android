# Adding Butterfly Access Credentials

Follow these steps to set up the environment variables required to download the Butterfly Imaging SDK, replacing `USERNAME` and `ACCESS_TOKEN` with the Butterfly **username** and **access token** provided during onboarding (note: these are not your email and password credentials):

## macOS

1. Open **Terminal** and run:
   ```sh
   echo 'export BFLY_SDK_USER_NAME="USERNAME"' >> ~/.zshenv
   echo 'export BFLY_SDK_ACCESS_TOKEN="ACCESS_TOKEN"' >> ~/.zshenv
   ```
1. Quit the **Android Studio** completely and reopen it for the new environment variables to take effect.

## Windows

1. Open **PowerShell** and run:
   ```powershell
   [System.Environment]::SetEnvironmentVariable("BFLY_SDK_USER_NAME", "USERNAME", "User")
   [System.Environment]::SetEnvironmentVariable("BFLY_SDK_ACCESS_TOKEN", "ACCESS_TOKEN", "User")
   ```
1. Restart Android Studio for the changes to take effect.
