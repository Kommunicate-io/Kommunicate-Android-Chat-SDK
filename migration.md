## Migrating to 2.5.0

- From version 2.5.0, we have removed Permissions from our SDK. This means if you need to use certain permissions like Camera, Storage, Location then you need to add it to your own app's AndroidManifest.xml file. 
- This feature is implemented because if you do not use certain features which require permissions, then it will not show up in Play Store or your app's Info.

- If you use Camera and Gallery Storage feature, add these permission:
```xml
<uses-permission
            android:name="android.permission.CAMERA"
            tools:node="merge" />
    
<uses-permission
        android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="29"
        tools:ignore="ScopedStorage"
        tools:node="merge" />
<uses-permission
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32"
        tools:node="merge" />

        <!--Permissions to be used when your app targets API 33 or higher-->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

```
- If you use Audio record / Speech to text feature, add these permissions:

```xml
<uses-permission
            android:name="android.permission.RECORD_AUDIO"
            tools:node="merge" />
```
- If you use Location feature, add these permissions:
```xml
        <uses-permission
            android:name="android.permission.ACCESS_COARSE_LOCATION"
            tools:node="merge" />
        <uses-permission
            android:name="android.permission.ACCESS_FINE_LOCATION"
            tools:node="merge" />
```

