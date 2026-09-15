# StealthPad

StealthPad is a comprehensive, security-focused productivity application for Android. It is designed to offer a balance between high-end privacy features and modern utility, such as artificial intelligence-driven content management. The application serves as both a daily note-taking tool and a secure digital vault for sensitive information.

## Detailed Application Overview

StealthPad is built on the principle of data sovereignty. Every piece of information a user creates—from note titles to text content—is treated as sensitive. The application utilizes hardware-backed security modules on the device to ensure that user data remains private even in the event of unauthorized access to the device's file system.

Beyond security, StealthPad integrates an AI engine that helps users process information faster. Whether it is generating new ideas, summarizing long meeting notes, or extracting actionable items, the app leverages cloud-based AI models while maintaining an encrypted communication channel.

## Key Functional Modules

### 1. Advanced Security and Encryption
The application implements a multi-layer security architecture:
*   **Android Keystore System**: Generates and stores cryptographic keys in a hardware-backed environment, making them inaccessible to other apps or the OS.
*   **AES-256 GCM Encryption**: All notes are encrypted using Advanced Encryption Standard in Galois/Counter Mode. GCM provides both confidentiality and data integrity (authentication), ensuring that the data has not been tampered with.
*   **Encrypted Local Database**: The Room database stores only the Base64-encoded encrypted strings, ensuring that the raw data never sits on the disk in plain text.

### 2. Private Vault
A dedicated section within the app for highly sensitive notes:
*   **Dual-Factor Access**: Requires both a custom PIN and optional biometric authentication (Fingerprint or Face Unlock).
*   **Auto-Lock Logic**: The vault automatically locks based on user-defined inactivity periods (Immediate, 1 minute, 5 minutes, or 15 minutes).
*   **Isolated Storage**: Vaulted notes are filtered out from the main dashboard and search results until the user successfully authenticates into the vault.

### 3. Stealth AI Engine
A suite of productivity tools powered by modern AI:
*   **Content Generation**: Users can provide a prompt, and the AI will draft a structured note, plan, or guide.
*   **Intelligent Summarization**: Processes long-form content to produce a concise summary while preserving the original context.
*   **Key Point Extraction**: Analyzes text to identify and list the most important takeaways and actionable items.
*   **Real-time Interaction**: A dedicated AI interface that supports importing existing notes for processing or exporting AI-generated content back into new notes.

### 4. Synchronization and Persistence
*   **Cloud Synchronization**: Notes are synced to a backend server using end-to-end encryption. The synchronization engine handles conflict resolution and ensures data parity across multiple devices.
*   **WorkManager Integration**: Sync tasks are handled in the background, allowing the app to retry failed operations and maintain data consistency even with intermittent network connectivity.
*   **Session Management**: Implements JWT-based authentication for secure API communication.

### 5. Media and Attachments
*   **Secure Attachments**: Support for image attachments that are stored in a private directory.
*   **Encryption at Rest**: Attachments are managed through a specialized storage manager that handles importing and secure deletion.

## User Interface and Experience

*   **Filing System UI**: A unique visual metaphor where notes are presented as "folder tabs," making categorization intuitive and visually distinct.
*   **Material 3 Design**: Follows the latest Google design standards for accessibility and aesthetic appeal.
*   **Lottie Animations**: Uses vector-based animations for state transitions (e.g., loading states, success messages, and empty states).
*   **Theming**: Robust support for Light and Dark modes, with a custom "Warm" palette designed to reduce eye strain.

## Technical Specifications

*   **Architecture**: MVVM (Model-View-ViewModel) with a Repository pattern for clean separation of concerns.
*   **Dependency Injection**: Dagger Hilt for managing object lifecycles and testing.
*   **Database**: Room Persistence Library with MediatorLiveData for complex data observations.
*   **Networking**: Retrofit 2 with OkHttp for RESTful API communication and interceptor-based JWT handling.
*   **Image Handling**: Glide for efficient image loading and caching.
*   **Tools**: Gradle Kotlin DSL, ViewBinding, and Jetpack Lifecycle components.

## Setup and Installation

### Prerequisites
*   Android Studio Ladybug or later.
*   Java Development Kit (JDK) 17.
*   A physical Android device or emulator running Android 7.0 (API 24) or higher.

### Installation Steps
1.  Clone this repository to your local machine.
2.  Open the project in Android Studio.
3.  Configure the AdMob Application ID in the AndroidManifest.xml file.
4.  Ensure you have a valid backend API URL configured in the Retrofit module.
5.  Sync the project with Gradle files and run the application.

---

*StealthPad - Privacy and Productivity Reimagined.*
