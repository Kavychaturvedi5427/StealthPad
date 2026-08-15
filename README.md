# StealthPad

A private, offline-first notes app for Android with on-device AES-256 encryption. Notes are encrypted before they ever touch local storage, using a key generated and held inside the Android Keystore — the key never leaves the device.

---

## Repository Structure

```text
StealthPad/
├── stealthpad-app/       # Android client (Java, MVVM, Room, Hilt, Retrofit)
└── stealthpad-backend/   # Spring Boot API (auth only, for now)
```

---

## How it works

- **Local-first storage** — Notes live in an on-device Room database. The app is fully usable without a network connection.
- **On-device encryption** — Each note is encrypted with `AES/GCM/NoPadding` before it's written to disk. The AES key is generated and stored in the **Android Keystore**, so raw key material never leaves secure hardware/OS storage and is never sent to the server.
- **Account layer** — A Spring Boot backend handles registration and login (JWT-based) so a user's notes are scoped to their account (`user_email`) locally. Note *content* itself is not yet synced to the backend — the server currently only handles auth.
- **Organization** — Notes can be tagged with a category (Personal, Work, Ideas, Secure, Important) and are timestamped.

---

## Tech Stack

### Android app (`stealthpad-app`)
- **Language:** Java
- **Architecture:** MVVM (`ViewModel` + `LiveData`)
- **DI:** Dagger Hilt
- **Local storage:** Room
- **Networking:** Retrofit2 + OkHttp (logging interceptor)
- **Encryption:** `javax.crypto` (AES/GCM) backed by Android Keystore
- **UI:** Material Components, ConstraintLayout, ViewBinding, Lottie animations
- **Build:** Gradle (Kotlin DSL), `compileSdk`/`targetSdk` 36, `minSdk` 24

### Backend (`stealthpad-backend`)
- **Framework:** Spring Boot (`spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`)
- **Auth:** JWT (`io.jsonwebtoken` / jjwt)
- **Database:** PostgreSQL
- **Mapping:** ModelMapper
- **Build:** Maven, Java 25

---

## Getting Started

### Prerequisites
- Android Studio (recent stable) with SDK 36
- JDK 17+ for the Android app; JDK 25 for the backend
- PostgreSQL instance for the backend
- Maven (or the bundled `mvnw`)

### 1. Clone the repository
```bash
git clone https://github.com/Kavychaturvedi5427/StealthPad.git
cd StealthPad
```

### 2. Run the backend
```bash
cd stealthpad-backend
```
Configure your database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/stealthpad
spring.datasource.username=your_username
spring.datasource.password=your_password
```
Then run:
```bash
./mvnw spring-boot:run
```

### 3. Run the Android app
Open the `stealthpad-app` folder in Android Studio, point `RetrofitClient`'s base URL at your running backend instance (e.g. `http://10.0.2.2:8080/` for the emulator), and run the app on a device or emulator.

---

## API

| Method | Endpoint             | Description                |
|--------|-----------------------|-----------------------------|
| POST   | `/api/auth/register`  | Create a new account        |
| POST   | `/api/auth/login`     | Authenticate and get a JWT  |

---

## Roadmap

- [ ] Sync encrypted note payloads to the backend (server storing ciphertext only)
- [ ] Biometric app lock
- [ ] In-app note search
- [ ] Soft-delete / trash for notes

---

yeh push kar do
