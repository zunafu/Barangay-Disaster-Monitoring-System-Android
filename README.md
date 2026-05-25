#  🚨 Disaster Monitoring & Response System

A full-featured web-based disaster management platform built for real-time incident reporting, monitoring, and emergency response coordination. The system allows citizens to submit disaster reports, government agencies to respond efficiently, and administrators to manage and oversee operations seamlessly.

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

### 👤 For Users
*Report incidents, view real-time map, confirm reports, and engage in discussions*

<img width="400" height="871" alt="image" src="https://github.com/user-attachments/assets/3258854d-0c85-4127-a4c1-de5c0ea2e43d" />

<img width="399" height="873" alt="image" src="https://github.com/user-attachments/assets/1577570f-d076-4888-89a0-e21e41c0d7ed" />

<img width="402" height="872" alt="image" src="https://github.com/user-attachments/assets/b1931281-effa-4b31-8d7e-7f0dd61d3522" />

<img width="400" height="872" alt="image" src="https://github.com/user-attachments/assets/2b306aac-7dc7-4f1e-8b37-ada45a100cc4" />

<img width="398" height="875" alt="image" src="https://github.com/user-attachments/assets/ef0febe6-f9bd-4cd3-877a-811525b73139" />

---


### 🏛️ For Barangay Officers
*Manage local incidents, update status, assign severity, and coordinate response*

<img width="402" height="877" alt="image" src="https://github.com/user-attachments/assets/17e8839e-e0ed-4426-a91f-26042f40fc52" />

<img width="400" height="876" alt="image" src="https://github.com/user-attachments/assets/3b1f9dac-0719-401b-9d02-c3e545167d91" />

<img width="405" height="876" alt="image" src="https://github.com/user-attachments/assets/04affe9e-179d-4a2a-94c9-7e05c0ebc4e9" />

<img width="404" height="873" alt="image" src="https://github.com/user-attachments/assets/483ae5af-002b-4c09-9226-f42473311b67" />

<img width="402" height="874" alt="image" src="https://github.com/user-attachments/assets/cf2617f3-0dc3-432f-a5a2-ff95c6063a02" />

<img width="400" height="877" alt="image" src="https://github.com/user-attachments/assets/42842c39-8328-4b99-912a-a84b9aba39f8" />


---

### 🔐 For Administrators
*Complete system control, user management, audit logs, and comprehensive analytics*

<img width="403" height="879" alt="image" src="https://github.com/user-attachments/assets/1bc2b51a-defe-43ff-a20c-3f6224f7156c" />

<img width="400" height="879" alt="image" src="https://github.com/user-attachments/assets/2821de65-ed95-4ffb-9dec-990e6d835b01" />

<img width="402" height="881" alt="image" src="https://github.com/user-attachments/assets/80a6ee83-1eb5-4ba0-8553-3fa7287ce4ad" />

<img width="404" height="880" alt="image" src="https://github.com/user-attachments/assets/dccac9f3-d616-4260-8868-deff8c0fb11d" />

<img width="399" height="870" alt="image" src="https://github.com/user-attachments/assets/003c65f1-559f-4dc9-8277-3522bbc70f30" />

<img width="400" height="881" alt="image" src="https://github.com/user-attachments/assets/ee516e42-95a5-48ac-9418-2ead9cf4115c" />


---

### ✨ More Features
*Community panel for Voting, Public Chat and Announcements and Account Settings*

<img width="396" height="876" alt="image" src="https://github.com/user-attachments/assets/8356a3f1-9253-4fd2-a8c7-84f7414b6ddb" />

<img width="403" height="878" alt="image" src="https://github.com/user-attachments/assets/fc82cd0d-8d26-4239-b486-b16a310a7933" />

<img width="401" height="876" alt="image" src="https://github.com/user-attachments/assets/e6631146-1bbf-48d5-b724-c5476a48b147" />

<img width="397" height="877" alt="image" src="https://github.com/user-attachments/assets/946fefc0-854b-4d7b-8aa4-9d60053fcb59" />

<img width="396" height="877" alt="image" src="https://github.com/user-attachments/assets/637b3c4c-8048-4f47-a505-ae1d947785e9" />




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

## 👥 Development Team

- **Franz Geoff Rivera**
- **Michael Ainjelo Maglaya**
- **Luisito Angelo Ocray**
- **Djanaisah Benito**

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

**Built with ❤️ for disaster preparedness and community safety.**
