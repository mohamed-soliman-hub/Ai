package com.aiphone.agent.core.tools

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

class ExtractTextFromImageTool @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseTool() {
    override val name = "extract_text_from_image"
    override val description = "Use OCR to extract all text from an image file"
    override val parameters = mapOf("path" to ToolParameter("Absolute path to the image file"))

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        val path = param(params, "path")
        return try {
            val file = File(path)
            require(file.exists()) { "Image file not found: $path" }
            val image = InputImage.fromFilePath(context, Uri.fromFile(file))
            val text = suspendCancellableCoroutine { cont ->
                recognizer.process(image)
                    .addOnSuccessListener { result -> cont.resume(result.text) }
                    .addOnFailureListener { cont.resume("") }
            }
            ToolExecutionResult(true, text.ifBlank { "No text found in image" }.take(3000), durationMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}
