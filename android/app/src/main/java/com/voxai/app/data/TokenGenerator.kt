package com.voxai.app.data

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Generates LiveKit-compatible JWT tokens on the client side.
 *
 * NOTE: This is for development convenience. In production, tokens should be
 * generated on a secure server to avoid exposing the API secret.
 */
object TokenGenerator {

    private const val TAG = "TokenGenerator"
    private val gson = Gson()

    /**
     * Generate a LiveKit-compatible JWT token.
     *
     * @param apiKey    LiveKit API Key
     * @param apiSecret LiveKit API Secret
     * @param roomName  Room to join
     * @param identity  Participant identity
     * @param name      Display name (optional)
     * @param ttl       Token time-to-live in seconds (default 2 hours)
     * @param agentName Agent dispatch name (optional). When set, the agent will be
     *                  automatically dispatched to the room on first creation.
     * @return JWT token string
     */
    fun generateToken(
        apiKey: String,
        apiSecret: String,
        roomName: String,
        identity: String,
        name: String = identity,
        ttl: Long = 7200L,
        agentName: String? = null,
    ): String {
        val now = System.currentTimeMillis() / 1000
        val exp = now + ttl

        Log.d(TAG, "=== Token Generation Debug ===")
        Log.d(TAG, "Input: apiKey=${apiKey.trim().take(8)}..., identity=$identity, room=$roomName, name=$name, agent=$agentName")
        Log.d(TAG, "Time: now=$now, exp=$exp, ttl=$ttl")

        // Header
        val header = JsonObject().apply {
            addProperty("alg", "HS256")
            addProperty("typ", "JWT")
        }

        // Video grant — match LiveKit CLI format
        val videoGrant = JsonObject().apply {
            addProperty("roomJoin", true)
            addProperty("room", roomName)
        }

        // Payload — LiveKit JWT claims (match CLI format)
        val payload = JsonObject().apply {
            addProperty("iss", apiKey.trim())
            addProperty("sub", identity)
            addProperty("exp", exp)
            addProperty("nbf", now)
            addProperty("iat", now)
            addProperty("identity", identity)
            addProperty("name", name)
            add("video", videoGrant)

            // Agent dispatch: when agentName is set, include roomConfig so the
            // agent is automatically dispatched when this room is first created.
            if (!agentName.isNullOrBlank()) {
                val agentDispatch = JsonObject().apply {
                    addProperty("agentName", agentName.trim())
                }
                val roomConfig = JsonObject().apply {
                    add("agents", JsonArray().apply { add(agentDispatch) })
                }
                add("roomConfig", roomConfig)
                Log.d(TAG, "Agent dispatch added: agentName=$agentName")
            }
        }

        val headerJson = gson.toJson(header)
        val payloadJson = gson.toJson(payload)

        Log.d(TAG, "Header JSON: $headerJson")
        Log.d(TAG, "Payload JSON: $payloadJson")

        val encodedHeader = base64UrlEncode(headerJson.toByteArray(StandardCharsets.UTF_8))
        val encodedPayload = base64UrlEncode(payloadJson.toByteArray(StandardCharsets.UTF_8))

        Log.d(TAG, "Encoded header: $encodedHeader")
        Log.d(TAG, "Encoded payload: $encodedPayload")

        val signingInput = "$encodedHeader.$encodedPayload"
        Log.d(TAG, "Signing input: $signingInput")

        val signature = hmacSha256(apiSecret.trim(), signingInput)
        val encodedSignature = base64UrlEncode(signature)

        Log.d(TAG, "Signature (hex): ${signature.joinToString("") { "%02x".format(it) }}")
        Log.d(TAG, "Encoded signature: $encodedSignature")

        val token = "$encodedHeader.$encodedPayload.$encodedSignature"
        Log.d(TAG, "Final token: $token")
        Log.d(TAG, "===============================")

        return token
    }

    private fun hmacSha256(key: String, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
