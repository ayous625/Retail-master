package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendChatMessage(
        model: GeminiModel,
        history: List<ChatMessage>,
        systemInstruction: String,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        // Try primary model first, fallback to gemini-3.8-flash or gemini-3.1-flash-lite if needed
        val candidateModels = listOf(model.modelId, "gemini-3.8-flash", "gemini-3.1-flash-lite").distinct()
        var lastError: Exception? = null

        for (modelId in candidateModels) {
            try {
                val result = executeRequest(modelId, apiKey, history, systemInstruction, userPrompt, model == GeminiModel.LIVE)
                if (result.isSuccess) {
                    return@withContext result
                } else {
                    lastError = result.exceptionOrNull() as? Exception
                }
            } catch (e: java.net.SocketTimeoutException) {
                lastError = Exception("Request timed out. Please check your internet connection and try again.")
                break // Don't chain timeouts if network is slow/unreachable
            } catch (e: java.net.UnknownHostException) {
                lastError = Exception("No internet connection available.")
                break
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Unable to reach Gemini AI service. Please check your connection."))
    }

    private fun executeRequest(
        modelId: String,
        apiKey: String,
        history: List<ChatMessage>,
        systemInstruction: String,
        userPrompt: String,
        isLive: Boolean
    ): Result<String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            // System Instruction (v1beta camelCase)
            if (systemInstruction.isNotBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
            }

            // Multi-turn Conversation Contents
            val contentsArray = JSONArray()
            for (msg in history.takeLast(10)) { // limit context to last 10 messages for speed & low latency
                val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", roleStr)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    })
                })
            }

            // Add current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", userPrompt))
                })
            })

            put("contents", contentsArray)

            // Generation Config
            put("generationConfig", JSONObject().apply {
                put("temperature", if (isLive) 0.5 else 0.7)
                put("maxOutputTokens", 1024)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(mediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody.isNullOrBlank()) {
            val errorMsg = parseErrorMessage(responseBody) ?: "HTTP ${response.code}: ${response.message}"
            Log.e("GeminiApiClient", "Request to $modelId failed: $errorMsg")
            return Result.failure(Exception(errorMsg))
        }

        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val replyText = parts.getJSONObject(0).optString("text", "")
                if (replyText.isNotBlank()) {
                    return Result.success(replyText)
                }
            }
        }

        return Result.failure(Exception("Model returned an empty response."))
    }

    private fun parseErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = JSONObject(body)
            val error = json.optJSONObject("error")
            error?.optString("message")
        } catch (e: Exception) {
            null
        }
    }
}
