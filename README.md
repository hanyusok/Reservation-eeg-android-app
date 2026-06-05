# Reservation-EEG Android App

An Android application for managing brainwave (EEG) test reservations, built with modern Android development practices.

## 🚀 Features

### 🏥 Clinic Information (Public)
*   **Clinic Overview**: Introduction to the EEG specialized clinic.
*   **Specialities**: View specialized medical fields (Sleep disorders, Epilepsy, etc.).
*   **Doctor Profiles**: Detailed information about medical staff.
*   **Contacts & Location**: Clinic address, phone, email, and operating hours.

### 🔐 Authentication & Profile
*   **Social Login**: Support for Google and Kakao social authentication.
*   **Email Login**: Traditional email/password sign-in and sign-up.
*   **Profile Management**: 
    *   View user information in a clean card format.
    *   Edit detailed profile information (Name, Resident ID, Address, Phone, etc.).
    *   Manual email entry for social accounts without email consent.

### 📅 Reservation System
*   **Step-by-step Flow**:
    1.  **Selection**: Choose desired EEG test types.
    2.  **Symptoms**: Input patient symptoms and history.
    3.  **Scheduling**: Select available time slots.
*   **My Reservations**: View and manage historical and upcoming reservations.

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose
*   **Navigation**: Jetpack Navigation Compose
*   **Architecture**: MVVM (ViewModel + StateFlow)
*   **Backend as a Service**: [Supabase](https://supabase.com/)
    *   **Authentication**: Supabase Auth (GoTrue)
    *   **Database**: Postgrest (PostgreSQL)
    *   **Realtime**: Realtime subscriptions for data sync.
*   **Networking**: Ktor Client
*   **Serialization**: Kotlinx Serialization
*   **Design**: Material 3

## ⚙️ Configuration

### Supabase Setup
The app requires a Supabase project. Configuration is located in `app/src/main/java/com/example/reservation_eeg_android_app/data/SupabaseClient.kt`.

1. Create a `profiles` table in your Supabase database.
2. Enable Email, Google, and Kakao providers in Authentication settings.
3. Add `supabase://callback` to the Redirect URIs in the Supabase Dashboard.

### Social Login Setup

#### Google Cloud Console
1. Create a Web Client ID.
2. Add `https://<YOUR_PROJECT_ID>.supabase.co/auth/v1/callback` to Authorized redirect URIs.
3. Add the Client ID and Secret to the Supabase Google provider.

#### Kakao Developers
1. Create an application.
2. Enable Android platform with package name `com.example.reservation_eeg_android_app`.
3. Enable Kakao Login and set Redirect URI to your Supabase callback URL.
4. Set "Kakao Account (Email)" to at least "Optional Consent" in Consent Items.

## 📱 Getting Started

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer recommended).
3. Sync Gradle projects.
4. Run the app on an emulator or physical device.

---
© 2024 Reservation-EEG App Team
