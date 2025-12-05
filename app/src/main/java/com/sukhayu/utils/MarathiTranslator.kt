package com.sukhayu.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class MarathiTranslator private constructor(context: Context) {

    private val dictionary = mutableMapOf<String, String>()

    // new reverse dictionary for English -> Marathi
    private val reverseDictionary = mutableMapOf<String, String>()

    init {
        loadDictionary(context)
    }

    /**
     * Load dictionary from JSON file and build reverse map.
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
                // build reverse map; if duplicate english words exist, keep the first mapping
                reverseDictionary.putIfAbsent(value, key)
            }

            inputStream.close()
            android.util.Log.d("MarathiTranslator", "Dictionary loaded with ${dictionary.size} words")
        } catch (e: Exception) {
            android.util.Log.e("MarathiTranslator", "Error loading dictionary", e)
            e.printStackTrace()
        }
    }

    // Regex to find words (Unicode letters); preserves punctuation and spacing
    // FIX: removed UNICODE_CHARACTER_CLASS flag (not supported on this Android runtime)
    private val wordPattern: Pattern = Pattern.compile("\\p{L}+")

    // Preserve capitalization for Latin words
    private fun preserveCapitalization(original: String, replacement: String): String {
        if (original.isEmpty() || replacement.isEmpty()) return replacement
        return if (original[0].isUpperCase()) {
            replacement.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else replacement
    }

    /**
     * Generic replace by map preserving non-letter characters and case where applicable.
     */
    private fun replaceWordsWithMap(
        text: String,
        map: Map<String, String>,
        caseInsensitiveLookup: Boolean = true
    ): String {
        val matcher = wordPattern.matcher(text)
        val sb = StringBuffer()
        while (matcher.find()) {
            val word = matcher.group()
            val lookupKey = if (caseInsensitiveLookup) word.lowercase() else word
            val replacementRaw =
                map[lookupKey] ?: map[word] ?: map[lookupKey.replaceFirstChar { it.lowercase() }]
            val replacement = if (replacementRaw != null) {
                preserveCapitalization(word, replacementRaw)
            } else {
                word
            }
            matcher.appendReplacement(sb, matcherSafe(replacement))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    // helper to escape $ and \ in replacement when using appendReplacement
    private fun matcherSafe(s: String): String {
        return s.replace("\\", "\\\\").replace("$", "\\$")
    }

    /**
     * Translate Marathi text to English
     */
    fun translateMarathiToEnglish(text: String): String {
        if (text.isEmpty()) return text
        // lookup keys as-is and lowercase keys in dictionary (JSON likely lowercase keys)
        // build a lowercase-key map for better matching
        val lowered = dictionary.mapKeys { it.key.lowercase() }
        return replaceWordsWithMap(text, lowered, caseInsensitiveLookup = true)
    }

    /**
     * Translate English text to Marathi
     */
    fun translateEnglishToMarathi(text: String): String {
        if (text.isEmpty()) return text
        // reverseDictionary keys may be English words; build lowercase lookup
        val lowered = reverseDictionary.mapKeys { it.key.lowercase() }
        return replaceWordsWithMap(text, lowered, caseInsensitiveLookup = true)
    }

    /**
     * Auto-detect script: if text contains Devanagari chars, assume Marathi -> English, else English -> Marathi
     */
    fun translateAuto(text: String): String {
        if (text.isEmpty()) return text
        val devanagariRange = "\\u0900-\\u097F"
        val devanagariPattern = Pattern.compile("[$devanagariRange]")
        val m = devanagariPattern.matcher(text)
        return if (m.find()) translateMarathiToEnglish(text) else translateEnglishToMarathi(text)
    }

    /**
     * Add new word to dictionaries at runtime (keeps both directions in sync).
     */
    fun addWord(marathi: String, english: String) {
        dictionary[marathi] = english
        // store reverse with english as key
        reverseDictionary[english] = marathi
    }

    /**
     * Recursive JSON translation preserving numeric fields (avoid converting numbers to strings).
     * numericFields: keys that should be treated as numeric if possible (e.g. "pageCount")
     */
    fun translateJsonPreserveTypes(jsonString: String, numericFields: Set<String> = setOf("pageCount")): String {
        try {
            val root = JSONObject(jsonString)
            val translated = translateJsonObject(root, numericFields)
            return translated.toString()
        } catch (e: Exception) {
            // fallback: return original if not a JSONObject, but try JSONArray
            try {
                val arr = JSONArray(jsonString)
                val translatedArr = translateJsonArray(arr, numericFields)
                return translatedArr.toString()
            } catch (ex: Exception) {
                android.util.Log.e("MarathiTranslator", "Failed to parse JSON for translation", e)
                return jsonString
            }
        }
    }

    private fun translateJsonObject(obj: JSONObject, numericFields: Set<String>): JSONObject {
        val keys = obj.keys()
        val out = JSONObject()
        while (keys.hasNext()) {
            val keyAny = keys.next()
            val key = keyAny as String
            val value = obj.get(key)
            when (value) {
                is JSONObject -> out.put(key, translateJsonObject(value, numericFields))
                is JSONArray -> out.put(key, translateJsonArray(value, numericFields))
                is Number -> out.put(key, value) // keep numbers as-is
                is String -> {
                    if (numericFields.contains(key)) {
                        // try to preserve numeric nature
                        val asLong = value.toLongOrNull()
                        if (asLong != null) out.put(key, asLong) else out.put(key, translateAuto(value))
                    } else {
                        out.put(key, translateAuto(value))
                    }
                }
                else -> out.put(key, value)
            }
        }
        return out
    }

    private fun translateJsonArray(arr: JSONArray, numericFields: Set<String>): JSONArray {
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val v = arr.get(i)
            when (v) {
                is JSONObject -> out.put(translateJsonObject(v, numericFields))
                is JSONArray -> out.put(translateJsonArray(v, numericFields))
                is Number -> out.put(v)
                is String -> out.put(translateAuto(v))
                else -> out.put(v)
            }
        }
        return out
    }

    /**
     * Translate a mutable map's String values in-place; preserves numeric fields if present.
     * Useful for converting payloads before passing to code that expects Long? etc.
     */
    fun translateMapValuesInPlace(
        map: MutableMap<String, Any?>,
        numericFields: Set<String> = setOf("pageCount")
    ) {
        val keys = map.keys.toList()
        for (k in keys) {
            val v = map[k]
            when (v) {
                is String -> {
                    if (numericFields.contains(k)) {
                        val asLong = v.toLongOrNull()
                        if (asLong != null) map[k] = asLong else map[k] = translateAuto(v)
                    } else {
                        map[k] = translateAuto(v)
                    }
                }
                is JSONObject -> map[k] = translateJsonObject(v, numericFields)
                is JSONArray -> map[k] = translateJsonArray(v, numericFields)
                is MutableMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    translateMapValuesInPlace(map[k] as MutableMap<String, Any?>, numericFields)
                }
                else -> {
                    // leave as is
                }
            }
        }
    }

    /**
     * Get dictionary sizes for both maps
     */
    fun getDictionarySize(): Int = dictionary.size
    fun getReverseDictionarySize(): Int = reverseDictionary.size

    /**
     * Return header label for given language state.
     * isMarathi == true -> Marathi label (in Devanagari)
     * isMarathi == false -> English label
     */
    fun getHeaderLanguageLabel(isMarathi: Boolean): String {
        return if (isMarathi) "मराठी" else "English"
    }

    /**
     * Return the opposite language boolean for toggling.
     */
    fun toggleLanguageFlag(currentIsMarathi: Boolean): Boolean {
        return !currentIsMarathi
    }

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
