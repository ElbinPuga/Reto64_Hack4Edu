import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SubirTomarFoto : ComponentActivity() {
    private lateinit var tflite: Interpreter
    private val numClasses = 54

    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkAndRequestPermissions()) {
            loadModel()
        }
        setContent {
            SeleccionarImagenDeGaleriaOTomarFoto()
        }
    }

    // Verificar y solicitar permisos
    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = mutableListOf<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), CAMERA_PERMISSION_CODE)
            false
        } else {
            true
        }
    }

    // Cargar el modelo de TFLite
    private fun loadModel() {
        try {
            val model = File(cacheDir, "modelo_letras.tflite")
            FileInputStream(model).channel.use { fileChannel ->
                val mappedByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
                tflite = Interpreter(mappedByteBuffer)
            }
        } catch (e: IOException) {
            Log.e("SubirTomarFoto", "Error loading model", e)
        }
    }

    // Función Composable para seleccionar imágenes o tomar una foto
    @Composable
    fun SeleccionarImagenDeGaleriaOTomarFoto() {
        var uriImagen by remember { mutableStateOf<Uri?>(null) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var letraDetectada by remember { mutableStateOf("") }
        val contexto = LocalContext.current

        // Lanzador para seleccionar imagen de la galería
        val lanzadorGaleria = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uriImagen = uri
            if (uri != null) {
                bitmap = loadBitmapFromUri(contexto, uri)
            }
        }

        // Lanzador para tomar una foto
        val lanzadorCamara = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success && uriImagen != null) {
                bitmap = loadBitmapFromUri(contexto, uriImagen!!)
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
                uriImagen = createTempImageUri(contexto)
                lanzadorCamara.launch(uriImagen!!)
            }) {
                Text(text = "Tomar Foto")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                letraDetectada = detectarLetra(bitmap)
            }) {
                Text(text = "Detectar Letra")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Letra detectada: $letraDetectada")
        }
    }

    // Función para cargar un bitmap desde un URI
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    // Función para detectar la letra
    private fun detectarLetra(bitmap: Bitmap?): String {
        if (bitmap == null) return ""

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * 64 * 64 * 3)
        byteBuffer.rewind()

        // Convertir el bitmap a ByteBuffer
        for (y in 0 until resizedBitmap.height) {
            for (x in 0 until resizedBitmap.width) {
                val pixel = resizedBitmap.getPixel(x, y)
                byteBuffer.putFloat((pixel shr 16 and 0xFF) / 255.0f)
                byteBuffer.putFloat((pixel shr 8 and 0xFF) / 255.0f)
                byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
            }
        }

        val inputArray = arrayOf(byteBuffer)
        val outputArray = Array(1) { FloatArray(numClasses) }

        // Realizar la predicción
        tflite.run(inputArray, outputArray)

        // Obtener la clase con mayor probabilidad
        val predictedLabel = indexOfMax(outputArray)
        return mapLabel(predictedLabel)
    }

    private fun indexOfMax(outputArray: Array<FloatArray>): Int {
        var maxIndex = 0
        var maxValue = outputArray[0][0]
        for (i in outputArray[0].indices) {
            if (outputArray[0][i] > maxValue) {
                maxValue = outputArray[0][i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    private fun mapLabel(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            6 -> "G"
            7 -> "H"
            8 -> "I"
            9 -> "J"
            10 -> "K"
            11 -> "L"
            12 -> "M"
            13 -> "N"
            14 -> "Ñ"
            15 -> "O"
            16 -> "P"
            17 -> "Q"
            18 -> "R"
            19 -> "S"
            20 -> "T"
            21 -> "U"
            22 -> "V"
            23 -> "W"
            24 -> "X"
            25 -> "Y"
            26 -> "Z"
            27 -> "a"
            28 -> "b"
            29 -> "c"
            30 -> "d"
            31 -> "e"
            32 -> "f"
            33 -> "g"
            34 -> "h"
            35 -> "i"
            36 -> "j"
            37 -> "k"
            38 -> "l"
            39 -> "m"
            40 -> "n"
            41 -> "ñ"
            42 -> "o"
            43 -> "p"
            44 -> "q"
            45 -> "r"
            46 -> "s"
            47 -> "t"
            48 -> "u"
            49 -> "v"
            50 -> "w"
            51 -> "x"
            52 -> "y"
            53 -> "z"
            else -> "Desconocido"
        }
    }

    private fun createTempImageUri(context: Context): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Nueva Foto")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
    }
}
