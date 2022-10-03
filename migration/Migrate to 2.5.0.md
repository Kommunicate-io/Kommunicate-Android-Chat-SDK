## Migrating to 2.5.0

- We have removed permissions permissions from our SDK. Now if you do not require Camera permissions in your chat app, then it will not show up in Permissions Required.
- Add these to your AndroidManifest.xml:

- For Camera:
<uses-permission
            android:name="android.permission.CAMERA"
            tools:node="merge" />

- For Audio:
<uses-permission
            android:name="android.permission.RECORD_AUDIO"
            tools:node="merge" />
            

- For Location:
 <uses-permission
            android:name="android.permission.ACCESS_COARSE_LOCATION"
            tools:node="merge" />
        <uses-permission
            android:name="android.permission.ACCESS_FINE_LOCATION"
            tools:node="merge" />

- For accessing gallery/storage:
<uses-permission
            android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            tools:ignore="ScopedStorage"
            tools:node="merge" />
        <uses-permission
            android:name="android.permission.READ_EXTERNAL_STORAGE"
            tools:node="merge" />