# 📱 Lost & Found – Android Application

Android application developed for the course  
**Development Of Applications For Mobile Operating Systems**

👨‍🏫 Lecturer: Tal Tzion

The application allows soldiers to report **lost or found equipment**, upload a photo, add a description, mark the location on a map, and help others return items quickly.

---

# 👨‍💻 Project Team

| Name | ID | Email |
|-----|-----|-----|
| Sean Nedorez | 213141146 | shonedo25@gmail.com |
| Shiran Levi | 324127315 | theshirkan@gmail.com |
| Liron Dabach | 322439027 | liron.dabach3@gmail.com |

---

# ⚙️ Main Features

- 📸 Upload lost / found item with photo
- 📝 Add description and contact details
- 📍 Save location using GPS
- 📰 View all reports in a central feed
- 🗺 View items on an interactive map
- 👤 User profile with personal posts
- ✏️ Edit and delete your own reports

---

# ☁️ Backend

Firebase Firestore is used as the backend database.

Firebase Console:  
https://console.firebase.google.com/project/eureka-564f3/settings/general/android:com.example.eureka

---

# 🛠 Technologies

- Kotlin
- Android SDK
- Firebase Firestore
- Google Maps API
- MVVM Architecture
- RecyclerView
- Navigation Component

---

## 📂 Project Structure

```
app
├── build
└── src
    ├── androidTest
    └── main
        ├── java/com.example.eureka
        │   ├── base
        │   ├── dao
        │   ├── features
        │   ├── fragments
        │   ├── models
        │   ├── utils
        │   ├── MainActivity
        │   └── MyApplication
        │
        └── res
            ├── layout
            │   ├── fragment_createpost.xml
            │   ├── fragment_login.xml
            │   ├── fragment_map.xml
            │   ├── fragment_post_detail.xml
            │   ├── fragment_post_list.xml
            │   ├── fragment_profile.xml
            │   ├── fragment_register.xml
            │   ├── post_row_layout.xml
            │   ├── posts_recycler_view.xml
            │   └── skeleton.xml
```

---

# 🚀 Application Flow

1. User logs in / registers  
2. Creates Lost or Found report  
3. Adds photo, description and location  
4. Post is saved in Firestore  
5. Other users can view it in the feed or map
