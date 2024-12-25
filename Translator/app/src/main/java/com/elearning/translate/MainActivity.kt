package com.elearning.translate

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.elearning.translate.R
import com.elearning.translate.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var items = arrayOf("English", "Vietnamese")
    private var conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    private lateinit var speechRecognizer: SpeechRecognizer
    private val speechRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Create the adapter for the AutoCompleteTextViews
        val itemsAdapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line, items
        )

        binding.languageFrom.setAdapter(itemsAdapter)
        binding.languageTo.setAdapter(itemsAdapter)

        // Set default selection for "languageFrom" (English) and "languageTo" (Vietnamese)
        binding.languageFrom.setText("English", false)  // Set "English" as default
        binding.languageTo.setText("Vietnamese", false)  // Set "Vietnamese" as default

        // Translate button functionality
        binding.translate.setOnClickListener {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(selectFrom())
                .setTargetLanguage(selectTo())
                .build()

            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    translator.translate(binding.input.text.toString())
                        .addOnSuccessListener { translatedText ->
                            binding.output.text = translatedText
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
        }

        // Voice input (Microphone) functionality
        binding.idMic.setOnClickListener {
            // Check if the speech recognizer is available
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Exchange button functionality (swap languages)
        binding.exchangeButton.setOnClickListener {
            swapLanguages()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now") // Optional prompt message
        }

        startActivityForResult(intent, speechRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == speechRequestCode && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                binding.input.setText(result[0]) // Set the first result as the input text
            }
        }
    }

    private fun selectFrom(): String {
        return when (binding.languageFrom.text.toString()) {
            "" -> TranslateLanguage.ENGLISH
            "English" -> TranslateLanguage.ENGLISH
            "Vietnamese" -> TranslateLanguage.VIETNAMESE
            else -> TranslateLanguage.ENGLISH
        }
    }

    private fun selectTo(): String {
        return when (binding.languageTo.text.toString()) {
            "" -> TranslateLanguage.VIETNAMESE
            "English" -> TranslateLanguage.ENGLISH
            "Vietnamese" -> TranslateLanguage.VIETNAMESE
            else -> TranslateLanguage.VIETNAMESE
        }
    }

    // Function to swap the languages
    private fun swapLanguages() {
        val fromLanguage = binding.languageFrom.text.toString()
        val toLanguage = binding.languageTo.text.toString()

        // Swap the languages
        binding.languageFrom.setText(toLanguage, false)
        binding.languageTo.setText(fromLanguage, false)
    }
}
