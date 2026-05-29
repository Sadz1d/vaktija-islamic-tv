# Vaktija TV — Islamic Prayer Times Display System

A two-app Android system for displaying Islamic prayer times and announcements on mosque TV screens. Built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

---

## Apps

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

## Architecture

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

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Auth | Firebase Authentication |
| Database | Firebase Realtime Database |
| Image hosting | Cloudinary (unsigned upload preset) |
| Image loading | Coil |
| Prayer times | Vaktija.ba REST API |
| Architecture | ViewModel + StateFlow |

---

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- A Firebase project
- A Cloudinary account (free tier is sufficient)

### 1. Clone the repository
```bash
git clone https://github.com/Sadz1d/vaktija-islamic-tv.git
cd vaktija-islamic-tv
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

## Firebase Rules Explained

- **TV app** can read content for its mosque (public read)
- **Admin app** can only write to the mosque linked to their account
- No admin can modify another mosque's data
- Admin profile data is readable only by the admin themselves

---

## TV App — First Launch

On first launch, the TV app shows a one-time setup screen asking for the **mosque ID** (e.g. `masline`). This is saved locally and never asked again. The mosque ID must match the one configured in Firebase under `/dzamije/{dzamijaId}`.

---

## Screenshots
<img width="1193" height="663" alt="loginTV" src="https://github.com/user-attachments/assets/76c53f75-f106-4e85-b953-5dcf948fcb6c" />
<img width="1199" height="679" alt="vaktijaSplitScreenTV" src="https://github.com/user-attachments/assets/270266e7-efa2-40a0-bb68-5842090a0448" />
<img width="705" height="393" alt="vaktijaFullScreenTV" src="https://github.com/user-attachments/assets/ec0671e5-e7d9-4865-ac47-bfad154aae01" />
<img width="738" height="1599" alt="viber_image_2026-05-30_01-31-15-007" src="https://github.com/user-attachments/assets/41d774f1-01b9-41b3-b2c8-609adcd8fc66" />
<img width="738" height="1599" alt="viber_image_2026-05-30_01-31-15-181" src="https://github.com/user-attachments/assets/22081c34-f782-4592-b2ed-776b6f870c00" />
<img width="738" height="1599" alt="viber_image_2026-05-30_01-31-15-111" src="https://github.com/user-attachments/assets/85b28933-3276-4942-bba6-540dbf458580" />

*Coming soon*

---

## Author

Built by [Sadžid Marić](https://github.com/Sadz1d)

> If you find this useful for your mosque, a ⭐ on GitHub is appreciated!

---

## License

© 2026 Sadžid Marić. All Rights Reserved.

This project is source-available for portfolio and educational purposes only.
Unauthorized copying, redistribution, or commercial use without explicit written
permission from the author is strictly prohibited.

For licensing inquiries: maricsadzid@gmail.com
