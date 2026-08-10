package com.shayshankrathore.irishvisadate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class NearestResult(val number: String, val decision: String, val difference: Int?) {
    val isApproved get() = decision.equals("APPROVED", ignoreCase = true)
}

data class VisaCheckResult(
    val applicationNumber: String,
    val found: Boolean,
    val decision: String? = null,
    val source: String? = null,
    val before: NearestResult? = null,
    val after: NearestResult? = null,
) {
    val isApproved get() = decision?.equals("APPROVED", ignoreCase = true) == true
    val isRefused  get() = decision?.equals("REFUSED", ignoreCase = true) == true
}

object VisaStatusApi {
    private const val BASE_URL = "https://shayshankrathore-ireland-visa-api.hf.space"
    private const val ENDPOINT = "$BASE_URL/api/check"

    suspend fun checkApplication(applicationNumber: String, embassyApiKey: String): Result<VisaCheckResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.setRequestProperty("Content-Type", "application/json")

                val requestBody = JSONObject()
                    .put("application_number", applicationNumber)
                    .put("embassy", embassyApiKey)
                    .toString()
                conn.outputStream.use { it.write(requestBody.toByteArray()) }

                val code = conn.responseCode
                if (code == 400) {
                    val errorText = conn.errorStream.bufferedReader().readText()
                    val err = JSONObject(errorText)
                    throw IllegalArgumentException(err.optString("detail", "Invalid application number"))
                }
                if (code != 200) throw IllegalStateException("Server error ($code)")

                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                conn.disconnect()

                val results = json.getJSONArray("results")
                var decision: String? = null
                var source: String? = null
                for (i in 0 until results.length()) {
                    val row = results.getJSONObject(i)
                    if (row.getString("embassy") == embassyApiKey) {
                        decision = row.getString("decision")
                        source = if (row.has("source")) row.getString("source") else null
                        break
                    }
                }

                if (decision != null) {
                    VisaCheckResult(
                        applicationNumber = json.getString("application_number"),
                        found = true,
                        decision = decision,
                        source = source
                    )
                } else {
                    val nearest = json.optJSONObject("nearest")
                    fun parseNearest(key: String) = nearest?.optJSONObject(key)?.let {
                        NearestResult(
                            number = it.getString("number"),
                            decision = it.getString("decision"),
                            difference = if (it.has("difference")) it.getInt("difference") else null
                        )
                    }
                    VisaCheckResult(
                        applicationNumber = json.getString("application_number"),
                        found = false,
                        before = parseNearest("before"),
                        after = parseNearest("after")
                    )
                }
            }
        }
}
