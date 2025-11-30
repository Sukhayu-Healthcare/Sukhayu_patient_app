package com.sukhayu.utils

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MarathiTranslator private constructor(context: Context) {

    private val dictionary = mutableMapOf<String, String>()

    init {
        loadDictionary(context)
    }

    /**
     * Load dictionary from JSON file
     */
    private fun loadDictionary(context: Context) {
        try {
            val resourceId = context.resources.getIdentifier("mr_en_dictionary", "raw", context.packageName)
            if (resourceId == 0) {
                android.util.Log.e("MarathiTranslator", "Dictionary file not found")
                return
            }
            
            val inputStream = context.resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val key = keys.next() as String
                val value = jsonObject.getString(key)
                dictionary[key] = value
            }
            
            inputStream.close()
            android.util.Log.d("MarathiTranslator", "Dictionary loaded with ${dictionary.size} words")
        } catch (e: Exception) {
            android.util.Log.e("MarathiTranslator", "Error loading dictionary", e)
            e.printStackTrace()
        }
    }

    /**
     * Translate Marathi text to English
     * Uses word-by-word replacement
     */
    fun translateMarathiToEnglish(text: String): String {
        if (text.isEmpty()) return text
        
        val words = text.split(" ")
        val translatedWords = words.map { word ->
            val cleanWord = word.trim()
            dictionary[cleanWord] ?: word
        }
        
        return translatedWords.joinToString(" ")
    }

    /**
     * Add new word to dictionary at runtime
     */
    fun addWord(marathi: String, english: String) {
        dictionary[marathi] = english
    }

    /**
     * Get dictionary size
     */
    fun getDictionarySize(): Int = dictionary.size

    companion object {
        @Volatile
        private var INSTANCE: MarathiTranslator? = null

        fun getInstance(context: Context): MarathiTranslator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MarathiTranslator(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
