package io.approov.chatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var submitButton: Button
    private lateinit var testButton: Button
    private val client = OkHttpClient().newBuilder().readTimeout(60, TimeUnit.SECONDS).build()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        submitButton = findViewById<Button>(R.id.submit)
        testButton = findViewById<Button>(R.id.testSubmit)

        val promptQuestion = findViewById<EditText>(R.id.prompt)
        val responseType = findViewById<TextView>(R.id.responseType)
        val responseText = findViewById<TextView>(R.id.responseText)

        submitButton.setOnClickListener {
            val question = promptQuestion.text.toString().trim()

            if (question.isEmpty()) {
                responseType.text = "ERROR:"
                responseText.text = "The Chat GPT prompt is empty..."
            } else {
                makeApiRequest(apiUrl, question) { response ->
                    runOnUiThread {
                        responseType.text = "CHAT GPT RESPONSE"
                        responseText.text  = response
                    }
                }
            }
        }

        testButton.setOnClickListener {
            val question = promptQuestion.text.toString().trim()

            if (question.isEmpty()) {
                responseType.text = "ERROR:"
                responseText.text = "The ChatGPT prompt is empty..."
            } else {
                makeApiRequest(apiUrlTest, question) { response ->
                    runOnUiThread {
                        responseType.text = "POSTMAN ECHO RESPONSE"
                        responseText.text = response
                    }
                }
            }
        }
    }

    fun makeApiRequest(apiUrl: String, prompt: String, callback: (String) -> Unit) {
        startProgress()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization",  Companion.apiKey)
            .post(buildRequestBody(prompt))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CHATGPT_APP","OpenAI API error response", e)
                Log.e("CHATGPT_APP", e.toString())
                stopProgress()
                callback(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                var body = response.body?.string()

                if (body == null) {
                    Log.v("CHATGPT_APP","OpenAI empty response")
                    body = "{}"
                } else {
                    Log.v("CHATGPT_APP", body)
                }

                val jsonBody = JSONObject(body)
                val response: String

                if (jsonBody.has("error")) {
                    response = jsonBody.getString("error")
                } else if (jsonBody.has("choices")) {
                    val jsonArray: JSONArray = jsonBody.getJSONArray("choices")
                    response = jsonArray.getJSONObject(0).getString("text")
                } else if (jsonBody.has("data")) {
                    response = jsonBody.getString("data")
                } else {
                    response = "Unknown result..."
                }

                stopProgress()
                callback(response)
            }
        })
    }

    private fun buildRequestBody(prompt: String): RequestBody {
        return """
            {
            "prompt": "$prompt",
            "max_tokens": 1000,
            "temperature": 5
            }
            """.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun startProgress() {
        runOnUiThread {
            submitButton.isEnabled = false
            testButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun stopProgress() {
        runOnUiThread {
            submitButton.isEnabled = true
            testButton.isEnabled = true
            progressBar.visibility = View.INVISIBLE
        }
    }
    companion object {
        private const val apiKey = "Bearer YOUR_CHATGPT_API_KEY"
        private const val apiUrl = "https://api.openai.com/v1/engines/text-davinci-003/completions"
        private const val apiUrlTest = "https://postman-echo.com/post"
    }
}