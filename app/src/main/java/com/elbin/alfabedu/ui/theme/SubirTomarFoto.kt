package com.elbin.alfabedu.ui.theme

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

class SubirTomarFoto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeleccionarImagenDeGaleriaOTomarFoto()
        }
    }
}

@Composable
fun SeleccionarImagenDeGaleriaOTomarFoto() {
    var uriImagen by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
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

        // Crear una URI temporal para la cámara
        Button(onClick = {
            uriImagen = createTempImageUri(contexto) // Crear URI temporal
            lanzadorCamara.launch(uriImagen!!) // Lanzar la cámara
        }) {
            Text(text = "Tomar Foto")
        }
    }
}

// Función para crear una URI temporal para la imagen
private fun createTempImageUri(context: Context): Uri {
    // Crear un URI temporal que no se almacene
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, "Nueva Foto")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
}
