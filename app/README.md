# ✅ VerifyMe

**VerifyMe** is an Android application that provides a secure login experience by verifying specific conditions based on device state. The app demonstrates dynamic condition checking before granting user access.

## Overview

The **VerifyMe** app ensures that users can log in only when certain device conditions are met, such as battery level, Wi-Fi connectivity, ambient light, Bluetooth connection, and temperature range. This project showcases Android's ability to interact with device hardware and APIs.

## Features

- **🔋 Battery Check:** Matches login credentials with the device's current battery percentage.
- **📶 Wi-Fi Connectivity:** Verifies the device is connected to a Wi-Fi network.
- **💡 Ambient Light:** Checks the ambient light level to ensure it matches the set condition.
- **📡 Bluetooth Connection:** Confirms a Bluetooth device is connected.
- **🌡️ Temperature Range:** Retrieves the external temperature and validates it within a specified range.

## How It Works

1. **Login Screen:** Users enter a password that corresponds to the device's current state.
2. **Condition Verification:** The app checks the following:
    - Battery level matches the entered password.
    - Wi-Fi connection is active.
    - Ambient light is within the required range.
    - Bluetooth device is connected.
    - Temperature is within an acceptable range.
3. **Success Page:** If all conditions are met, the user is granted access and sees a success message.
