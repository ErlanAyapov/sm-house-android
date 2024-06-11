package com.example.smhouse

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smhouse.api.ApiClient
import com.example.smhouse.api.ApiService
import com.example.smhouse.api.Command
import com.example.smhouse.api.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var editTextCommand: EditText
    private lateinit var buttonSend: Button
    private lateinit var textViewResult: TextView
    private lateinit var buttonVoiceInput: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextCommand = findViewById(R.id.editTextCommand)
        buttonSend = findViewById(R.id.buttonSend)
        textViewResult = findViewById(R.id.textViewResult)
        buttonVoiceInput = findViewById(R.id.buttonVoiceInput)
        val backgroundImageView: ImageView = findViewById(R.id.backgroundImageView)

        // Загрузка локального GIF из ресурсов с помощью Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable._mf6) // Замените на имя вашего GIF-файла без расширения
            .into(backgroundImageView)

        buttonSend.setOnClickListener {
            val commandText = editTextCommand.text.toString()
            if (commandText.isNotEmpty()) {
                sendCommand(commandText)
            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show()
            }
        }

        buttonVoiceInput.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the command")

        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE_INPUT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Voice input not supported on your device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VOICE_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!result.isNullOrEmpty()) {
                    editTextCommand.setText(result[0])
                }
            } else {
                Toast.makeText(this, "Voice input canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendCommand(commandText: String) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val command = Command(commandText)

        val call = apiService.sendCommand(command)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    textViewResult.text = "Status: ${apiResponse?.status}"
                } else {
                    textViewResult.text = "Request failed: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                val message = when (t) {
                    is IOException -> "Network Error: ${t.message}"
                    is HttpException -> "HTTP Error: ${t.message}"
                    else -> "Unexpected Error: ${t.message}"
                }
                textViewResult.text = "Request failed: $message"
            }
        })
    }

    companion object {
        private const val REQUEST_CODE_VOICE_INPUT = 100
    }
}
