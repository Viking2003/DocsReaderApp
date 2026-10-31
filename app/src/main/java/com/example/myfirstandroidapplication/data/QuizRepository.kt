package com.example.myfirstandroidapplication.data

import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.QuizDifficulty
import com.example.myfirstandroidapplication.models.QuizQuestion

object QuizRepository {

    val jniQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = 1,
            category = DocCategory.JNI,
            questionText = "What is the mandatory first argument passed to every C/C++ JNI function implementation?",
            options = listOf(
                "jobject thiz",
                "JNIEnv* env",
                "JavaVM* vm",
                "jclass clazz"
            ),
            correctAnswerIndex = 1,
            explanation = "Every JNI C/C++ function receives JNIEnv* env as its first argument, which provides access to the JNI function interface table.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 2,
            category = DocCategory.JNI,
            questionText = "Which JNI function MUST be called after GetStringUTFChars to prevent native memory leaks?",
            options = listOf(
                "FreeStringUTFChars",
                "DeleteLocalRef",
                "ReleaseStringUTFChars",
                "CloseString"
            ),
            correctAnswerIndex = 2,
            explanation = "ReleaseStringUTFChars informs the JVM that the C++ code no longer needs the native UTF-8 string memory buffer.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 3,
            category = DocCategory.JNI,
            questionText = "What happens if a native C++ loop creates 1000 JNI local references without releasing them?",
            options = listOf(
                "They are automatically garbage collected immediately",
                "The JNI local reference table capacity (512) overflows and crashes the app",
                "The references convert to global references automatically",
                "Nothing, local references use zero memory"
            ),
            correctAnswerIndex = 1,
            explanation = "JNI local references have a limited table capacity (typically 512). Failing to call DeleteLocalRef inside tight loops causes an overflow crash.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 4,
            category = DocCategory.JNI,
            questionText = "How can a background C++ thread safely obtain a JNIEnv pointer to invoke Java/Kotlin methods?",
            options = listOf(
                "Pass the main thread's JNIEnv pointer across threads",
                "Call JavaVM->AttachCurrentThread(&env, nullptr)",
                "Create a new JNIEnv manually with new JNIEnv()",
                "JNI cannot be called from background threads"
            ),
            correctAnswerIndex = 1,
            explanation = "JNIEnv pointers are thread-local. Background C++ threads must attach to the JVM using AttachCurrentThread before using JNIEnv.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 5,
            category = DocCategory.JNI,
            questionText = "When a Java/Kotlin method called from C++ throws an exception, what does JNI do?",
            options = listOf(
                "C++ execution automatically unwinds with a C++ exception",
                "The JVM instantly terminates the entire process",
                "JNI sets a pending exception flag; C++ must check ExceptionCheck() and handle it",
                "The exception is ignored and discarded"
            ),
            correctAnswerIndex = 2,
            explanation = "JNI does not unwind C++ stack frames automatically. C++ must check env->ExceptionCheck() after method calls.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 6,
            category = DocCategory.JNI,
            questionText = "Which keyword is used in Kotlin to declare a method implemented in native C/C++?",
            options = listOf(
                "native fun",
                "external fun",
                "cdecl fun",
                "jni fun"
            ),
            correctAnswerIndex = 1,
            explanation = "In Kotlin, native methods are declared using the 'external' modifier keyword.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 7,
            category = DocCategory.JNI,
            questionText = "How do you load a shared native library (.so) into a Kotlin class?",
            options = listOf(
                "System.loadLibrary(\"library-name\")",
                "Runtime.load(\"library-name\")",
                "Context.loadNative(\"library-name\")",
                "Library.load(\"library-name\")"
            ),
            correctAnswerIndex = 0,
            explanation = "System.loadLibrary(\"library-name\") loads the compiled shared native library into the ART runtime.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 8,
            category = DocCategory.JNI,
            questionText = "What is the difference between a local reference and a global reference in JNI?",
            options = listOf(
                "Local references last forever; global references expire immediately",
                "Local references are automatically freed when native methods return; global references persist until explicitly deleted",
                "Global references can only be used on the main thread",
                "There is no difference"
            ),
            correctAnswerIndex = 1,
            explanation = "Local references are automatically freed on method return, whereas global references must be manually deleted using DeleteGlobalRef.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 9,
            category = DocCategory.JNI,
            questionText = "What C++ macro prevents name mangling when exporting functions to JNI?",
            options = listOf(
                "extern \"C\"",
                "__cdecl",
                "JNI_EXPORT",
                "NO_MANGLING"
            ),
            correctAnswerIndex = 0,
            explanation = "extern \"C\" instructs the C++ compiler to use C linkage, preventing function name mangling so the JVM can locate JNI symbols.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 10,
            category = DocCategory.JNI,
            questionText = "What data structure does JNI use to look up native functions registered dynamically?",
            options = listOf(
                "JNINativeMethod array",
                "JavaMethodTable",
                "FunctionPointerMap",
                "RuntimeRegistry"
            ),
            correctAnswerIndex = 0,
            explanation = "Dynamic registration uses an array of JNINativeMethod structures mapping Java method names, signatures, and native function pointers.",
            difficulty = QuizDifficulty.HARD
        )
    )

    val androidQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = 11,
            category = DocCategory.ANDROID_RULES,
            questionText = "In Unidirectional Data Flow (UDF) architecture, how do Events and State flow?",
            options = listOf(
                "Events flow DOWN from ViewModels, State flows UP from UI",
                "Events flow UP (UI ➔ ViewModel ➔ Repository), State flows DOWN (Repository ➔ ViewModel ➔ UI)",
                "Events and State flow sideways between Activities",
                "Events and State are stored in static singletons"
            ),
            correctAnswerIndex = 1,
            explanation = "In UDF, user interactions (Events) travel UP into business logic, while reactive screen data (State) flows DOWN to the UI.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 12,
            category = DocCategory.ANDROID_RULES,
            questionText = "Which component should NEVER be stored in long-lived static variables or singletons?",
            options = listOf(
                "ApplicationContext",
                "Activity or View Context",
                "Primitive integers",
                "Immutable data classes"
            ),
            correctAnswerIndex = 1,
            explanation = "Holding static references to Activity or View Context prevents garbage collection during screen rotation, causing severe memory leaks.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 13,
            category = DocCategory.ANDROID_RULES,
            questionText = "Which Coroutine Dispatcher should be used for main-safe disk I/O and network operations?",
            options = listOf(
                "Dispatchers.Main",
                "Dispatchers.Default",
                "Dispatchers.IO",
                "Dispatchers.Unconfined"
            ),
            correctAnswerIndex = 2,
            explanation = "Dispatchers.IO is optimized for disk reading, database transactions, and network calls off the UI thread.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 14,
            category = DocCategory.ANDROID_RULES,
            questionText = "On Android 12+ (API 31+), what attribute MUST be declared explicitly for manifest components with intent-filters?",
            options = listOf(
                "android:exported=\"true\" or \"false\"",
                "android:permission=\"required\"",
                "android:directBootAware=\"true\"",
                "android:enabled=\"true\""
            ),
            correctAnswerIndex = 0,
            explanation = "Android 12 mandates explicit android:exported declarations for all activities, services, and receivers containing intent-filters.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 15,
            category = DocCategory.ANDROID_RULES,
            questionText = "What is the minimum recommended touch target size according to Material Design guidelines?",
            options = listOf(
                "24dp x 24dp",
                "32dp x 32dp",
                "48dp x 48dp",
                "64dp x 64dp"
            ),
            correctAnswerIndex = 2,
            explanation = "Material Design specifies a minimum touch target size of 48dp x 48dp to ensure accessibility for all users.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 16,
            category = DocCategory.ANDROID_RULES,
            questionText = "Which Android Jetpack component survives configuration changes and holds screen state?",
            options = listOf(
                "BroadcastReceiver",
                "ViewModel",
                "ContentProvider",
                "IntentService"
            ),
            correctAnswerIndex = 1,
            explanation = "ViewModel is designed to store and manage UI-related data conscious of lifecycle changes such as screen rotations.",
            difficulty = QuizDifficulty.EASY
        ),
        QuizQuestion(
            id = 17,
            category = DocCategory.ANDROID_RULES,
            questionText = "How long can blocking the Android Main (UI) thread before triggering an ANR dialog?",
            options = listOf(
                "1 second",
                "2 seconds",
                "5 seconds",
                "10 seconds"
            ),
            correctAnswerIndex = 2,
            explanation = "Blocking the Main Thread for 5 or more seconds results in an Application Not Responding (ANR) error dialog.",
            difficulty = QuizDifficulty.MEDIUM
        ),
        QuizQuestion(
            id = 18,
            category = DocCategory.ANDROID_RULES,
            questionText = "Which class should be used to securely store encrypted credentials and auth tokens on Android?",
            options = listOf(
                "EncryptedSharedPreferences",
                "Plaintext SharedPreferences",
                "Global FileOutputStream",
                "SQLite raw database"
            ),
            correctAnswerIndex = 0,
            explanation = "EncryptedSharedPreferences encrypts keys and values using Android KeyStore, safeguarding sensitive user credentials.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 19,
            category = DocCategory.ANDROID_RULES,
            questionText = "What is the purpose of SavedStateHandle in a ViewModel?",
            options = listOf(
                "To make network requests in the background",
                "To persist UI state across process death and system reclamation",
                "To communicate between two separate Activities",
                "To render Jetpack Compose UI nodes"
            ),
            correctAnswerIndex = 1,
            explanation = "SavedStateHandle allows ViewModels to save and restore key-value data when the OS kills the app process in the background.",
            difficulty = QuizDifficulty.HARD
        ),
        QuizQuestion(
            id = 20,
            category = DocCategory.ANDROID_RULES,
            questionText = "Which API enables smooth page-swiping transitions between pages in modern Android apps?",
            options = listOf(
                "ViewPager2",
                "Legacy GalleryView",
                "HorizontalScrollView",
                "ListView"
            ),
            correctAnswerIndex = 0,
            explanation = "ViewPager2 is the modern Jetpack widget supporting horizontal and vertical swiping with RecyclerView backing.",
            difficulty = QuizDifficulty.EASY
        )
    )
}