# ================================================
# ApnaSandesh — Production ProGuard Rules
# ================================================

# ======== Room Database ========
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase$Callback { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# ======== Room Entities & Models ========
-keep class com.apnasandesh.app.core.database.** { *; }
-keep class com.apnasandesh.app.core.model.** { *; }

# ======== Dagger / Hilt ========
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keepclasseswithmembernames class * {
    @dagger.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# ======== Hilt Entry Points (BroadcastReceiver) ========
-keep @dagger.hilt.EntryPoint interface * { *; }
-keep class com.apnasandesh.app.domain.transport.SmsStatusBroadcastReceiver$ReceiverEntryPoint { *; }

# ======== WorkManager ========
-keep class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep @androidx.hilt.work.HiltWorker class * { *; }

# ======== Kotlin ========
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class kotlin.reflect.jvm.internal.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.debug.**

# ======== Jetpack Compose ========
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ======== DataStore ========
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# ======== AndroidX Lifecycle ========
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ======== AndroidX Navigation ========
-keep class * extends androidx.navigation.Navigator { *; }

# ======== Enums (critical for Room/model enums) ========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ======== BroadcastReceiver ========
-keep class com.apnasandesh.app.domain.transport.SmsStatusBroadcastReceiver { *; }

# ======== ForegroundService ========
-keep class com.apnasandesh.app.domain.campaign.CampaignSendingForegroundService { *; }

# ======== Application class ========
-keep class com.apnasandesh.app.ApnaSandeshApplication { *; }

# ======== Suppress common warnings ========
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn java.lang.invoke.**
