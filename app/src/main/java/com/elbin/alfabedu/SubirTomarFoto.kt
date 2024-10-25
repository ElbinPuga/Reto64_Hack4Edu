package com.elbin.alfabedu.ui.theme
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.io.FileInputStream
import java.nio.ByteOrder

class SubirTomarFoto : ComponentActivity() {
    private lateinit var tflite: Interpreter
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeleccionarImagenDeGaleriaOTomarFoto()
        }
        tflite = Interpreter(loadModelFile())

        // Inicializar TTS en español
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "ES") // Cambiar a español de España
            }
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = assets.openFd("modelo_letras_v2.0.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun detectarLetra(bitmap: Bitmap): Int {
        val inputData = prepareInputData(bitmap)
        val outputData = Array(1) { FloatArray(54) } // Cambia esto según tu número de clases

        tflite.run(inputData, outputData)

        return outputData[0].indexOfMax()
    }

    private fun prepareInputData(bitmap: Bitmap): ByteBuffer {
        val inputSize = 64 // Tamaño de la entrada según tu modelo
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val mutableBitmap = bitmap.copy(Bitmap.Config.RGBA_F16, true)
        val resizedBitmap = Bitmap.createScaledBitmap(mutableBitmap, inputSize, inputSize, true)

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resizedBitmap.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }

        return byteBuffer
    }

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        for (i in 1 until size) {
            if (this[i] > this[maxIndex]) {
                maxIndex = i
            }
        }
        return maxIndex
    }

    // Función para hacer que TTS diga la letra en español
    fun speak(letra: String) {
        val texto = "La letra trazada es: $letra"
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

@Composable
fun SeleccionarImagenDeGaleriaOTomarFoto() {
    var uriImagen by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var letraDetectada by remember { mutableStateOf<String?>(null) }
    val contexto = LocalContext.current

    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uriImagen = uri
        if (uri != null) {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contexto.contentResolver, uri)
            } else {
                val fuente = ImageDecoder.createSource(contexto.contentResolver, uri)
                ImageDecoder.decodeBitmap(fuente)
            }
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && uriImagen != null) {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contexto.contentResolver, uriImagen)
            } else {
                val fuente = ImageDecoder.createSource(contexto.contentResolver, uriImagen!!)
                ImageDecoder.decodeBitmap(fuente)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { lanzadorGaleria.launch("image/*") }) {
            Text(text = "Seleccionar Imagen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            uriImagen = createTempImageUri(contexto) // Crear URI temporal
            lanzadorCamara.launch(uriImagen!!) // Lanzar la cámara
        }) {
            Text(text = "Tomar Foto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            bitmap?.let { bmp ->
                val activity = contexto as SubirTomarFoto
                val letraIndex = activity.detectarLetra(bmp) // Clasificar la imagen
                letraDetectada = mapIndexToLetter(letraIndex) // Mapear el índice a la letra

                // Hacer que TTS diga la letra detectada
                activity.speak(letraDetectada ?: "")
            }
        }) {
            Text(text = "Detectar")
        }

        letraDetectada?.let { letra ->
            Text("Letra detectada: $letra")
        }
    }
}

// Función para mapear el índice de salida a letras
private fun mapIndexToLetter(index: Int): String {
    val letras = listOf(
        "A", "a", "B", "b", "C", "c", "D", "d", "E", "e",
        "F", "f", "G","g","H", "h", "I", "i", "J", "j", "K", "k",
        "L", "l", "M", "m", "N", "n", "Ñ", "ñ", "O", "o",
        "P", "p", "Q", "q", "R", "r", "S","s", "T", "t", "U",
        "u", "V","v", "W", "w", "X", "x", "Y", "y", "Z", "z",
    )

    return if (index in letras.indices) letras[index] else "Desconocido"
}

// Función para crear una URI temporal para la imagen
private fun createTempImageUri(context: Context): Uri {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, "Nueva Foto")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
}