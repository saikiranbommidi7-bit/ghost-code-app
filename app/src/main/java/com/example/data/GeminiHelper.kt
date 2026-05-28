package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val TAG = "GeminiHelper"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if the user's API Key is properly configured and active.
     */
    fun isKeyConfigured(): Boolean {
        val key = apiKey
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * Asynchronously queries Gemini to ask for explanation, feedback, or dynamic programming hints.
     */
    suspend fun getExplanation(prompt: String): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext getOfflineFriendlyExplanation(prompt)
        }

        val jsonMedia = "application/json; charset=utf-8".toMediaType()
        // Escaping common escape sequences
        val escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val bodyJson = """
            {
              "contents": [{
                "parts": [{
                  "text": "$escapedPrompt"
                }]
              }],
              "generationConfig": {
                "temperature": 0.5,
                "maxOutputTokens": 800
              }
            }
        """.trimIndent()

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMedia))
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorMsg = response.body?.string() ?: ""
                    Log.e(TAG, "API failed with code: $code. Error: $errorMsg")
                    return@withContext "👻 Ghost AI is sleeping (Response Code $code). Here is a friendly hint: Remember to inspect variable typing and verify your output constraints or try again shortly!"
                }

                val responseBody = response.body?.string() ?: ""
                val textResponse = parseTextFromResponse(responseBody)
                if (textResponse.isNotEmpty()) {
                    textResponse
                } else {
                    "👻 Ghost Code compiled your request but found no text returned. Make sure your variables are properly assigned."
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network failure calling Gemini API", e)
            "👻 Ghost AI had an offline delay: ${e.localizedMessage}. Please check your internet connection."
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing Gemini API", e)
            "👻 Ghost AI had an unexplained spectral hiccup: ${e.localizedMessage}"
        }
    }

    /**
     * Parsing helper to pull text from Gemini API response body without requiring bulky plugins.
     */
    private fun parseTextFromResponse(json: String): String {
        try {
            // Find "text": "..." inside candidates
            val textToken = "\"text\":"
            var index = json.indexOf(textToken)
            if (index == -1) return ""
            
            index += textToken.length
            // find the start quotation mark of the string value
            val startQuote = json.indexOf("\"", index)
            if (startQuote == -1) return ""
            
            // Extract the string value paying attention to escaped quotes
            val result = StringBuilder()
            var i = startQuote + 1
            while (i < json.length) {
                val c = json[i]
                if (c == '"') {
                    // verify it is not escaped
                    if (json[i - 1] == '\\') {
                        result.append(c)
                    } else {
                        // end of text
                        break
                    }
                } else if (c == '\\') {
                    // special escaped characters inside response
                    if (i + 1 < json.length) {
                        val next = json[i + 1]
                        when (next) {
                            'n' -> result.append("\n")
                            't' -> result.append("\t")
                            'r' -> result.append("\r")
                            '"' -> result.append("\"")
                            '\\' -> result.append("\\")
                            else -> result.append(next)
                        }
                        i++
                    }
                } else {
                    result.append(c)
                }
                i++
            }
            return result.toString().trim()
        } catch (e: Exception) {
            Log.e(TAG, "Regex/Indices error parsing JSON response", e)
            return ""
        }
    }

    /**
     * Fallback explanation generator for offline support.
     */
    private fun getOfflineFriendlyExplanation(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("variable") || p.contains("var") -> """
                👻 **Ghost Code Offline Explanation (Variables)**:
                
                In programming, a **variable** is a storage name that points to a specific memory slot:
                
                * **Dynamic Typing** (e.g. Python, JS): You just assign it: `num = 10`. The interpreter figures out the type!
                * **Static Typing** (e.g. Kotlin): Types are strict: `val listSize: Int = 10`. Kotlin also provides type inference: `val score = 300`.
                
                *Need dynamic explanations? Register your GEMINI_API_KEY inside the Secrets panel of AI Studio to wake the live Ghost AI tutor!*
            """.trimIndent()

            p.contains("loop") || p.contains("for") || p.contains("while") -> """
                👻 **Ghost Code Offline Explanation (Loops)**:
                
                **Loops** let you cast repeatable spells across data collections without repeating code lines:
                
                * **For-loops**: Best when you know exactly how many loops to execute. In Python: `for i in range(5):`.
                * **While-loops**: Repeats as long as a boolean check holds `True`. Be careful of **infinite loops** which devour your phone's memory!
                
                *Add your GEMINI_API_KEY to the AI Studio Secrets panel to get detailed, customized live loops code debugging!*
            """.trimIndent()

            p.contains("function") || p.contains("def") -> """
                👻 **Ghost Code Offline Explanation (Functions)**:
                
                A **function** is an encapsulated block of code designed to do a single duty:
                
                - Declared in Python using `def login_user(username):`
                - Declared in Kotlin using `fun loginUser(username: String): Boolean`
                
                You group routines in functions to make code readable and reusable.
            """.trimIndent()

            else -> """
                👻 **Ghost Code Live Assistant**:
                
                Hello apprentice! I can explain variables, functions, and loops.
                
                To enable live AI explanations, write code snippets, and debug them in real-time, please configure your **GEMINI_API_KEY** in the Secrets Panel of AI Studio. It will instantly connect this app to Google's live server!
            """.trimIndent()
        }
    }
}
