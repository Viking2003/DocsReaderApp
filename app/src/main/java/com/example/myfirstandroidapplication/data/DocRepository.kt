package com.example.myfirstandroidapplication.data

import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.DocPage

object DocRepository {

    val jniPages: List<DocPage> = listOf(
        DocPage(
            id = 1,
            category = DocCategory.JNI,
            chapterTitle = "Chapter 1: Overview of JNI",
            subtitle = "Bridging Kotlin/Java JVM with Native C/C++ Code • 3 min read",
            bodyText = """
                The Java Native Interface (JNI) is a native programming interface provided by the Java Virtual Machine (JVM / Android ART). It enables Kotlin and Java code running inside the Managed Runtime to interoperate with applications and libraries written in C, C++, or Assembly.
                
                Why use JNI in Android?
                1. Performance-Critical Workloads: Math-heavy image processing, game engine physics, audio synthesis, or AI inference engines.
                2. Legacy Native Code Reuse: Reusing existing C/C++ libraries (such as OpenCV, FFmpeg, or SQLite) without rewriting them in Kotlin.
                3. Direct Hardware Access: Interfacing with low-level device drivers or platform-specific APIs not exposed by the Java SDK.
                
                JNI Memory Bridge Model:
                • JVM Managed Heap: Garbage-collected objects (Kotlin/Java instances).
                • Native Heap: C++ allocated memory via malloc/new. Unmanaged and subject to manual memory management.
            """.trimIndent(),
            codeSnippet = """
                // Kotlin side: Load shared native library (.so)
                class NativeBridge {
                    companion object {
                        init {
                            System.loadLibrary("native-lib")
                        }
                    }
                    external fun calculateFastHash(data: String): Long
                }
            """.trimIndent(),
            codeTitle = "KOTLIN JNI DECLARATION",
            keyTakeaway = "JNI introduces execution overhead due to context switches between JVM and native code. Only use JNI when native performance or existing C++ libraries are truly required."
        ),
        DocPage(
            id = 2,
            category = DocCategory.JNI,
            chapterTitle = "Chapter 2: Native Method Signatures",
            subtitle = "Function Name Mapping & Parameter Marshalling • 4 min read",
            bodyText = """
                When Kotlin invokes an 'external fun', the ART runtime searches for a matching C/C++ function name using a standardized JNI naming convention.
                
                Name Formatting Rules:
                1. Prefix: Starts with 'Java_'
                2. Package Path: Fully qualified package name where '.' is replaced by '_'
                3. Class Name: Target class name appended with '_'
                4. Method Name: The exact method name declared in Kotlin.
                
                Mandatory First Two Arguments:
                • JNIEnv* env: Pointer to the JNI environment function table. Gives access to JNI API calls.
                • jobject thiz (or jclass clazz): Represents the Kotlin instance calling the method (or jclass for companion object/static methods).
            """.trimIndent(),
            codeSnippet = """
                #include <jni.h>
                
                extern "C" JNIEXPORT jlong JNICALL
                Java_com_example_myfirstandroidapplication_NativeBridge_calculateFastHash(
                    JNIEnv* env,
                    jobject thiz,
                    jstring data) {
                    
                    const char* nativeString = env->GetStringUTFChars(data, nullptr);
                    // Process nativeString in C++ ...
                    env->ReleaseStringUTFChars(data, nativeString);
                    return 42L;
                }
            """.trimIndent(),
            codeTitle = "C++ NATIVE IMPLEMENTATION",
            keyTakeaway = "Always ensure C++ function signatures match the exact fully qualified Kotlin package name, class name, and method name."
        ),
        DocPage(
            id = 3,
            category = DocCategory.JNI,
            chapterTitle = "Chapter 3: JNI Types & String Conversion",
            subtitle = "Mapping JVM Primitive Types & String Lifecycle • 4 min read",
            bodyText = """
                JNI defines C equivalents for all Java primitive types:
                • jboolean (uint8_t)   • jint (int32_t)
                • jbyte (int8_t)       • jlong (int64_t)
                • jchar (uint16_t)     • jfloat (float)
                • jshort (int16_t)     • jdouble (double)
                
                String Marshalling:
                Java strings (jstring) are UTF-16 objects managed by JVM heap. C++ expects null-terminated char* strings (UTF-8).
                
                Conversion Steps:
                1. Acquire UTF-8 string: env->GetStringUTFChars(jstr, nullptr)
                2. Use the C string in native logic.
                3. Release UTF-8 string: env->ReleaseStringUTFChars(jstr, cstr)
                
                Failing to release GetStringUTFChars leads to permanent native memory leaks!
            """.trimIndent(),
            codeSnippet = """
                // Safe String Conversion Helper
                std::string parseJniString(JNIEnv* env, jstring jStr) {
                    if (!jStr) return "";
                    const char* chars = env->GetStringUTFChars(jStr, nullptr);
                    std::string result(chars);
                    env->ReleaseStringUTFChars(jStr, chars);
                    return result;
                }
            """.trimIndent(),
            codeTitle = "SAFE C++ STRING HELPER",
            keyTakeaway = "Every GetStringUTFChars MUST be paired with a corresponding ReleaseStringUTFChars call to prevent memory leaks."
        ),
        DocPage(
            id = 4,
            category = DocCategory.JNI,
            chapterTitle = "Chapter 4: JNI Local & Global References",
            subtitle = "Managing JVM Heap References from C++ • 5 min read",
            bodyText = """
                JNI handles jobject references differently from normal C pointers:
                
                1. Local References (jobject, jstring, jclass, jarray):
                • Automatically created when returning objects from JNI calls.
                • Valid ONLY during the current native method invocation on the current thread.
                • Automatically freed when native method returns to Java.
                • Problem: JNI local reference table has a default capacity limit (typically 512). Loops creating objects will overflow the table and crash the app!
                
                2. Global References:
                • Created explicitly via env->NewGlobalRef(obj).
                • Remains valid across multiple native method calls and threads until explicitly deleted via env->DeleteGlobalRef(globalObj).
            """.trimIndent(),
            codeSnippet = """
                // Preventing Local Reference Leak in Loops
                for (int i = 0; i < 1000; i++) {
                    jstring item = env->NewStringUTF(env, "item");
                    // Do work with item ...
                    env->DeleteLocalRef(item); // Free immediately inside loop!
                }
            """.trimIndent(),
            codeTitle = "LOCAL REF CLEANUP IN LOOPS",
            keyTakeaway = "Delete local references inside tight loops using DeleteLocalRef to avoid JNI local reference table overflow crashes."
        ),
        DocPage(
            id = 5,
            category = DocCategory.JNI,
            chapterTitle = "Chapter 5: Threading & Exception Safety",
            subtitle = "Native Threads, AttachCurrentThread & Exception Checks • 4 min read",
            bodyText = """
                Threading Rules in JNI:
                • JNIEnv pointers are thread-local! Never share a JNIEnv* across multiple POSIX / std::thread instances.
                • To interact with JVM from a background C++ thread, attach the thread to JavaVM:
                  JavaVM->AttachCurrentThread(&env, nullptr)
                • Always detach before thread termination: JavaVM->DetachCurrentThread().
                
                Exception Handling in JNI:
                When a Java/Kotlin method called from C++ throws an exception, C++ execution does NOT automatically unwind!
                • Check for pending exceptions: env->ExceptionCheck()
                • Clear pending exception: env->ExceptionClear()
                • Throw C++ exception or handle gracefully.
            """.trimIndent(),
            codeSnippet = """
                // Safe Exception Check Pattern
                jvalue result = env->CallObjectMethod(obj, methodId);
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe(); // Print stacktrace
                    env->ExceptionClear();    // Clear pending exception
                    return;                   // Return safely
                }
            """.trimIndent(),
            codeTitle = "JNI EXCEPTION HANDLING",
            keyTakeaway = "Always check env->ExceptionCheck() after invoking Kotlin/Java methods via JNI before using returned results."
        )
    )

    val androidRulesPages: List<DocPage> = listOf(
        DocPage(
            id = 6,
            category = DocCategory.ANDROID_RULES,
            chapterTitle = "Rule 1: Modern Android Architecture",
            subtitle = "MVVM / MVI, Unidirectional Data Flow & Layering • 4 min read",
            bodyText = """
                Modern Android App Architecture mandates a clean separation of concerns into distinct layers:
                
                1. UI Layer (Views / Jetpack Compose):
                • Displays application state on screen.
                • Renders state reactively using StateFlow, LiveData, or Compose State.
                • Handles user events and passes them down to ViewModels.
                
                2. ViewModel Layer:
                • Holds screen state and business logic.
                • Survives configuration changes (e.g., screen rotation).
                • Emits immutable StateFlow streams.
                
                3. Data Layer (Repositories & Data Sources):
                • Manages application data logic and network/database fetching (Room, Retrofit).
                • Single Source of Truth (SSOT) pattern.
                
                Unidirectional Data Flow (UDF):
                Events flow UP (UI ➔ ViewModel ➔ Repository) and State flows DOWN (Repository ➔ ViewModel ➔ UI).
            """.trimIndent(),
            codeSnippet = """
                // Unidirectional Data Flow Example
                class ArticleViewModel(
                    private val repository: ArticleRepository
                ) : ViewModel() {
                
                    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
                    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
                    
                    fun loadArticles() {
                        viewModelScope.launch {
                            _uiState.value = UiState.Success(repository.getArticles())
                        }
                    }
                }
            """.trimIndent(),
            codeTitle = "UNIDIRECTIONAL DATA FLOW (UDF)",
            keyTakeaway = "Never place database calls, network requests, or heavy business logic directly inside Activities or Fragments."
        ),
        DocPage(
            id = 7,
            category = DocCategory.ANDROID_RULES,
            chapterTitle = "Rule 2: Component Lifecycles & Leaks",
            subtitle = "Activity States, Memory Leaks & SavedStateHandle • 5 min read",
            bodyText = """
                Understanding the Android Lifecycle is essential to prevent memory leaks, app crashes, and battery drain.
                
                Core Lifecycle States:
                • onCreate(): Initial setup, inflating layouts, injecting dependencies.
                • onStart(): Activity becoming visible.
                • onResume(): Activity in foreground, interactive with user.
                • onPause(): Activity partially obscured or losing focus.
                • onStop(): Activity no longer visible.
                • onDestroy(): Final cleanup before destruction.
                
                Golden Rules to Prevent Memory Leaks:
                1. Never store long-lived static references to Context, View, or Activity.
                2. Use ApplicationContext for singletons, or weak references if needed.
                3. Always cancel Coroutine scopes or unregister listeners in onCleared() / onDestroy().
                4. Use SavedStateHandle in ViewModels to persist state across system-initiated process death.
            """.trimIndent(),
            codeSnippet = """
                // Safe ViewModel with SavedStateHandle
                class ReaderViewModel(
                    private val savedState: SavedStateHandle
                ) : ViewModel() {
                    // Persists selected page index across process death
                    var currentPage: Int
                        get() = savedState.get<Int>("PAGE") ?: 0
                        set(value) { savedState["PAGE"] = value }
                }
            """.trimIndent(),
            codeTitle = "SAVED STATE PERSISTENCE",
            keyTakeaway = "Never pass Activity Context into ViewModels or long-lived singletons to avoid permanent memory leaks."
        ),
        DocPage(
            id = 8,
            category = DocCategory.ANDROID_RULES,
            chapterTitle = "Rule 3: Performance & Coroutines",
            subtitle = "Main Thread Responsiveness & Offloading Tasks • 4 min read",
            bodyText = """
                Android UI runs on a single Main Thread (UI Thread). Blocking the Main Thread for more than 16 milliseconds causes dropped frames (jank), and blocking for 5 seconds triggers an Application Not Responding (ANR) dialog!
                
                Coroutine Dispatchers Guide:
                • Dispatchers.Main: UI interactions, updating views, calling suspend functions.
                • Dispatchers.IO: Disk I/O, network requests, reading local files, Room database operations.
                • Dispatchers.Default: Heavy CPU computations, parsing large JSON data, image processing.
                
                Best Practices:
                • Mark suspend functions as main-safe using withContext(Dispatchers.IO).
                • Avoid blocking calls like Thread.sleep() or synchronous I/O on Dispatchers.Main.
            """.trimIndent(),
            codeSnippet = """
                // Main-Safe Data Fetching
                class NewsRepository {
                    suspend fun fetchDocument(): String = withContext(Dispatchers.IO) {
                        // Network or File I/O here ...
                        return@withContext "Document Content"
                    }
                }
            """.trimIndent(),
            codeTitle = "MAIN-SAFE COROUTINE FUNCTION",
            keyTakeaway = "Every suspend function in a repository or data source should be main-safe using withContext."
        ),
        DocPage(
            id = 9,
            category = DocCategory.ANDROID_RULES,
            chapterTitle = "Rule 4: Android Security & Intents",
            subtitle = "Intent Redirection, Exported Components & Permissions • 5 min read",
            bodyText = """
                Security must be built into Android applications from day one.
                
                Component Export Rules (Android 12+ / API 31+):
                • Every <activity>, <service>, or <receiver> in AndroidManifest.xml that defines an <intent-filter> MUST explicitly declare android:exported="true" or "false".
                • Set android:exported="false" for internal app components to prevent malicious third-party apps from launching them directly.
                
                Preventing Intent Redirection Vulnerabilities:
                • Never untrustingly unpack and launch nested Intents received from getIntent().getParcelableExtra().
                • Validate target component package names before calling startActivity().
                
                Secure Storage:
                • Use EncryptedSharedPreferences or Jetpack DataStore with Android KeyStore for storing auth tokens and sensitive credentials.
            """.trimIndent(),
            codeSnippet = """
                <!-- Secure Manifest Component Declaration -->
                <activity
                    android:name=".InternalDetailActivity"
                    android:exported="false"> <!-- Protected from external apps -->
                </activity>
            """.trimIndent(),
            codeTitle = "SECURE MANIFEST DECLARATION",
            keyTakeaway = "Always set android:exported=\"false\" on internal activities to block unauthorized intent access from malicious apps."
        ),
        DocPage(
            id = 10,
            category = DocCategory.ANDROID_RULES,
            chapterTitle = "Rule 5: UI/UX & Accessibility",
            subtitle = "Touch Targets, Edge-to-Edge & Contrast Guidelines • 4 min read",
            bodyText = """
                A great Android app delivers an inclusive, accessible, and delighting user experience.
                
                Accessibility Checklist (Material Design 3):
                1. Touch Target Sizes: Ensure all clickable buttons and interactive views have a minimum touch target size of 48dp x 48dp.
                2. Content Descriptions: Provide informative android:contentDescription attributes for all decorative or action ImageViews and ImageButtons.
                3. High Color Contrast: Ensure readable text contrast ratios (minimum 4.5:1 for body text).
                4. Responsive & Adaptive Layouts: Design layouts using ConstraintLayout or Compose FlexBox that scale across phones, foldables, and tablets.
                5. Dynamic Font Scaling: Test layouts with system font scaling set to 200% to ensure text does not clip or overlap.
            """.trimIndent(),
            codeSnippet = """
                <!-- Accessible Touch Target ImageButton -->
                <ImageButton
                    android:id="@+id/btnBookmark"
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:contentDescription="@string/cd_bookmark_page"
                    android:src="@drawable/ic_bookmark" />
            """.trimIndent(),
            codeTitle = "ACCESSIBLE TOUCH TARGET",
            keyTakeaway = "Always test your app with TalkBack screen reader and dynamic text scaling enabled to ensure full accessibility."
        )
    )

    fun getAllPages(): List<DocPage> = jniPages + androidRulesPages
}