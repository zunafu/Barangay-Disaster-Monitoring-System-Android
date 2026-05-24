# Disaster Management App

A comprehensive Android application built with Jetpack Compose designed for real-time disaster reporting, tracking, and community coordination. The app leverages open-source mapping and local persistence to provide a reliable tool for emergency situations.

## 📱 Features

- **Interactive Incident Map**: Real-time visualization of disaster incidents (Fire, Flood, Earthquake, etc.) using OSMDroid.
- **Role-Based Access Control**:
  - **Admin**: Full system oversight, user management, and detailed audit logs.
  - **Barangay**: localized management and verification of incidents within their jurisdiction.
  - **User**: Report incidents by pinning locations, confirm reports from others, and manage profiles.
- **Incident Reporting**: Easy-to-use interface for reporting disasters with severity levels and descriptions.
- **Community Chat**: Real-time chat system for community-wide coordination during disasters.
- **Verification System**: Community-driven "voting" to confirm the validity of reported incidents.
- **Audit Logging**: Comprehensive tracking of all system actions (logins, reports, updates) for transparency.
- **Local Persistence**: Full offline capability for viewing previously loaded data using Room SQLite database.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Local Database**: Room (SQLite)
- **Maps**: OSMDroid (OpenStreetMap)
- **Image Loading**: Coil
- **Architecture**: MVVM (Model-View-ViewModel) logic with Repository pattern.
- **Design System**: Material Design 3

## 📸 Screenshots

| Map View | Incident Report | Community Chat |
| :---: | :---: | :---: |
| ![Map Screen](screenshots/map_view.png) | ![Report Dialog](screenshots/report.png) | ![Chat Screen](screenshots/chat.png) |

*(Note: Please add your actual screenshots to a `screenshots/` directory in the root of the project)*

## 🚀 Installation Guide

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK 24+ (Android 7.0+)

### Setup Steps
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/disastermanagement.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select `Open` and navigate to the cloned project folder.
3. **Sync Gradle**:
   - Wait for the project to finish indexing and sync Gradle files.
4. **Permissions**:
   - The app requires Location permissions to show your position on the map and report incidents.
5. **Run**:
   - Connect an Android device or start an emulator and click the `Run` button.

## 🔐 Test Accounts

For testing purposes, the app is pre-populated with the following administrative accounts. For standard user access, please use the registration feature.

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@test.com` | `password123` |
| **Barangay** | `barangay@test.com` | `password123` |
| **Standard User** | *Create via Register* | *User defined* |

### How to add a Standard User:
1. Open the app and navigate to the **Login** screen.
2. Tap on **Register** or **Sign Up**.
3. Fill in your details and click **Register**.
4. You can then log in with your newly created credentials to access user-specific features like reporting incidents.

## 📂 Project Structure

- `ui/screens/`: Contains all Compose-based screens (Main, Login, User Management, etc.).
- `data/database/`: Room database configuration, Entity definitions (Incident, User, AuditLog, Chat), and DAOs.
- `utils/`: Helper classes for Audit Logging and Location services.
- `ui/theme/`: Material 3 theme configurations.

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
