#  Vaktija TV — Islamic Prayer Times Display System

A two-app Android system for displaying Islamic prayer times and announcements on mosque TV screens. Built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

---

##  Apps

### Vaktija TV (Display App)
Designed for Android TV screens or tablets mounted in mosques. Shows:
- Live prayer times fetched from the [Vaktija.ba](https://vaktija.ba) API
- Real-time countdown to the next prayer
- Admin-uploaded announcements (images) on the right half of the screen
- Automatically switches between full-screen prayer times (when no announcements) and split-screen layout

### Vaktija Admin (Admin App)
A phone app for mosque administrators to manage announcements:
- Secure Firebase Authentication login
- Upload/manage announcement images via Cloudinary
- Toggle announcements active/inactive
- Changes reflect on the TV screen in real-time

---

##  Architecture

```
Firebase Realtime Database
└── dzamije/
    └── {dzamijaId}/
        └── content/        ← announcements per mosque

admins/
└── {uid}/
    ├── dzamijaId: "mosque-id"
    └── naziv: "Mosque Name"
```

- **Multi-tenant**: One Firebase project supports multiple mosques. Each admin is linked to their mosque via their Firebase UID.
- **TV Setup**: On first launch, the TV app prompts for a mosque ID (saved locally). No re-configuration needed after that.
- **Real-time sync**: Firebase listeners update the TV screen instantly when admin makes changes.

---

##  Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Auth | Firebase Authentication |
| Database | Firebase Realtime Database |
| Image hosting | Cloudinary (unsigned upload preset) |
| Image loading | Coil |
| Prayer times | Vaktija.eu REST API |
| Architecture | ViewModel + StateFlow |

---

##  Setup

### Prerequisites
- Android Studio Hedgehog or newer
- A Firebase project
- A Cloudinary account (free tier is sufficient)

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/vaktija-tv.git
cd vaktija-tv
```

### 2. Firebase setup
1. Create a new project at [Firebase Console](https://console.firebase.google.com)
2. Add two Android apps to the project:
   - `com.yourpackage.display` (TV app)
   - `com.yourpackage.admin` (Admin app)
3. Download `google-services.json` for each app and place them in:
   - `vaktijaTV/app/google-services.json`
   - `vaktijaAdmin/app/google-services.json`

> A template is provided at `app/google-services.json.example.txt`

4. Enable **Realtime Database** and **Authentication → Email/Password** in Firebase Console

5. Set up Realtime Database structure:
```json
{
  "admins": {
    "ADMIN_UID": {
      "dzamijaId": "your-mosque-id",
      "naziv": "Your Mosque Name"
    }
  },
  "dzamije": {
    "your-mosque-id": {
      "content": {}
    }
  }
}
```

6. Set Firebase Security Rules (Realtime Database):
```json
{
  "rules": {
    "admins": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": false
      }
    },
    "dzamije": {
      "$dzamijaId": {
        "content": {
          ".read": true,
          ".write": "auth != null && root.child('admins').child(auth.uid).child('dzamijaId').val() === $dzamijaId"
        }
      }
    }
  }
}
```

### 3. Cloudinary setup
1. Create a free account at [Cloudinary](https://cloudinary.com)
2. Create an **unsigned upload preset** named `islamic_tv` (or your own name)
3. Update `CloudinaryManager.kt`:
```kotlin
private val cloudName = "YOUR_CLOUD_NAME"
private val uploadPreset = "YOUR_UPLOAD_PRESET"
```

### 4. Build and run
Open each project folder in Android Studio and run on your target device.

---

##  Firebase Rules Explained

- **TV app** can read content for its mosque (public read)
- **Admin app** can only write to the mosque linked to their account
- No admin can modify another mosque's data
- Admin profile data is readable only by the admin themselves

---

##  TV App — First Launch

On first launch, the TV app shows a one-time setup screen asking for the **mosque ID** (e.g. `masline`). This is saved locally and never asked again. The mosque ID must match the one configured in Firebase under `/dzamije/{dzamijaId}`.

---

##  Screenshots

*Coming soon*

---

##  Contributing

This project was built for real-world use in local mosques. If you'd like to adapt it for your community, feel free to fork it. Pull requests are welcome.

---

##  License

MIT License — free to use, modify and distribute.

---

##  Author

Built by [Sadžid Marić](https://github.com/Sadz1d)

> If you find this useful for your mosque, a ⭐ on GitHub is appreciated!
