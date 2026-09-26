# Aturan R8 tambahan buat build release (isMinifyEnabled = true di app/build.gradle.kts).
# Library utama (Retrofit, OkHttp, kotlinx.serialization, Room, Hilt, Firebase) udah bawa
# consumer rules sendiri - file ini cuma buat yang gak ke-cover otomatis.

# Stack trace crash tetap kebaca (nama file + nomor baris), nama aslinya disamarkan.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit baca generic type & anotasi lewat reflection (mis. ApiResponse<List<...>>).
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# kotlinx.serialization: serializer DTO (data/remote/dto) diakses lewat companion
# .serializer() - dijaga biar gak kebuang/ke-rename.
-keepclassmembers @kotlinx.serialization.Serializable class com.example.sakuku.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.example.sakuku.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Credential Manager (Google Sign-In): provider Play Services diload lewat reflection.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
    *;
}
