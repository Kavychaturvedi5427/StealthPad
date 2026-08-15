```markdown
# StealthPad

A private, offline-first notes application powered by **Zero-Knowledge / End-to-End Encryption (E2EE)**. 

StealthPad encrypts all your notes client-side before they ever touch the network or database. The server stores only encrypted blobs and metadata—meaning no one, not even the server host, can read your data without your master password.

---

## Security Architecture (Zero-Knowledge)

* **Client-Side Encryption:** All note content and titles are encrypted/decrypted locally on the device using standard cryptography (e.g., AES-256-GCM / XChaCha20).
* **Key Derivation:** Encryption keys are derived client-side from the master password using a secure key derivation function (e.g., Argon2id / PBKDF2).
* **Zero-Knowledge Backend:** The backend handles authentication, synchronization, and storage of ciphertext blobs without ever receiving raw keys or plain text.
* **Offline-First:** Read, create, and edit notes completely offline. Changes are stored locally and synchronized automatically when an internet connection is established.

---

## 📁 Repository Structure

```text
StealthPad/
├── stealthpad-app/       # Frontend client (Local crypto, offline storage, UI editor)
└── stealthpad-backend/   # Backend API (Encrypted blob sync, JWT auth, database)

```

---

## Features

* **Zero-Knowledge Security:** True End-to-End Encryption where keys never leave the client device.
* **Offline-First Workflow:** Full access to create, edit, and search notes without an active internet connection.
* **Seamless Encrypted Sync:** Conflict-safe synchronization of encrypted payloads across devices.
* **JWT-Based Authentication:** Extended 30-day session tokens for uninterrupted workflow across restarts.
* **Decoupled Architecture:** Clean separation between the client-side cryptographic engine and the sync server.

---

##  Tech Stack

* **Frontend (`stealthpad-app`):** Modern Web / Mobile framework (React, React Native, or Electron), Web Crypto API / Libsodium, Local Database (IndexedDB / SQLite / WatermelonDB).
* **Backend (`stealthpad-backend`):** Node.js / Express (or FastAPI) REST API for encrypted blob persistence.
* **Authentication:** JSON Web Tokens (JWT).
* **Database:** PostgreSQL / MongoDB / SQLite.

---

##  Getting Started

### Prerequisites

* [Node.js](https://nodejs.org/) (v18+ recommended) or [Python](https://www.python.org/)
* [Git](https://git-scm.com/)

---

### 1. Clone the Repository

```bash
git clone [https://github.com/Kavychaturvedi5427/StealthPad.git](https://github.com/Kavychaturvedi5427/StealthPad.git)
cd StealthPad

```

---

### 2. Backend Setup (`stealthpad-backend`)

1. Navigate to the backend directory:
```bash
cd stealthpad-backend

```


2. Install dependencies:
```bash
npm install
# OR: pip install -r requirements.txt

```


3. Configure your environment variables (`.env`):
```env
PORT=5000
DATABASE_URL=your_database_url
JWT_SECRET=your_jwt_secret_key

```


4. Start the server:
```bash
npm run dev
# OR: npm start

```



---

### 3. Frontend Setup (`stealthpad-app`)

1. Open a new terminal and navigate to the frontend directory:
```bash
cd stealthpad-app

```


2. Install dependencies:
```bash
npm install

```


3. Configure API endpoints:
```env
VITE_API_URL=http://localhost:5000
# OR: REACT_APP_API_URL=http://localhost:5000

```


4. Run the development server:
```bash
npm run dev
# OR: npm start

```



---

##  Synchronization & Encryption Flow

```
+-------------------------------------------------------------+
|                      Client (Device)                        |
|  [Master Password] -> [Key Derivation (Argon2id/PBKDF2)]    |
|                                │                            |
|                                ▼                            |
|  [Plaintext Note]  -> [Local Encrypt (AES-256/XChaCha20)]   |
|                                │                            |
|                                ▼                            |
|  [Local DB (Offline Cache)] <- [Ciphertext Blob]            |
+--------------------------------┬----------------------------+
                                 │ Sync (Ciphertext Only)
                                 ▼
+-------------------------------------------------------------+
|                    StealthPad Backend Server                |
|  - Stores: { user_id, note_id, ciphertext, iv, salt, sync_tag } |
|  - Cannot decrypt or read content                           |
+-------------------------------------------------------------+

```
