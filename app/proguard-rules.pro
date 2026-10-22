# Room SQLite ProGuard Rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-dontwarn androidx.room.paging.**

# Dagger / Hilt ProGuard Rules
-dontwarn com.google.errorprone.annotations.**
-keep class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Entities and DAOs
-keep class com.localreach.bizsms.core.database.** { *; }
-keep class com.localreach.bizsms.core.model.** { *; }
