package com.elbin.alfabedu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elbin.alfabedu.ui.theme.AlfabeduTheme
import com.elbin.alfabedu.ui.theme.ColorAmarillo
import com.elbin.alfabedu.ui.theme.ColorVerde
import com.elbin.alfabedu.ui.theme.FuentePoppins
import com.elbin.alfabedu.ui.theme.SubirTomarFoto
import com.elbin.alfabedu.ui.theme.formaCardBotton
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verifico el estado de onboarding completado
        val sharedPref: SharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPref.getBoolean("isOnboardingCompleted", false)

        // Si el onboarding ya está completado, s e inicia la actividad SubirTomarfoto
        if (isOnboardingCompleted) {
            val intent = Intent(this, SubirTomarFoto::class.java)
            startActivity(intent)
            finish() // Cierra MainActivity
            return
        }

        setContent {
            AlfabeduTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val items = listOf(
                        OnBoardingData(
                            R.drawable.pantalla1,
                            colorFondo = Color(0xFF008AC3),
                            colorPrincipal = Color(0xFF00B5EA),
                            textoPrincipal = "Aprende",
                            subTexto = "¡Diviértete con las letras!"
                        ),
                        OnBoardingData(
                            R.drawable.nino_escribiendo_yellowbg,
                            colorFondo = Color(0xFFFAD206),
                            colorPrincipal = ColorAmarillo,
                            textoPrincipal = "Muestra tus dibujos",
                            subTexto = "Toma fotos de tus letras"
                        ),
                        OnBoardingData(
                            R.drawable.ninos_saltando_bggreen,
                            colorFondo = Color(0xFF8DDD66),
                            colorPrincipal = ColorVerde,
                            textoPrincipal = "Mejora tus letras",
                            subTexto = "Dibuja y vamos a hacerlas más lindas"
                        )
                    )

                    val pagerState = rememberPagerState(
                        pageCount = items.size,
                        initialOffscreenLimit = 2,
                        infiniteLoop = false,
                        initialPage = 0
                    )

                    OnBoardingPager(
                        item = items,
                        pagerState = pagerState,
                        onComplete = {
                            // Al completar el onboarding, guardar el estado en sharedPreferences
                            with(sharedPref.edit()) {
                                putBoolean("isOnboardingCompleted", true)
                                apply()
                            }
                            // se inicia la actividad SubirTomarFoto para cpose
                            val intent = Intent(this@MainActivity, SubirTomarFoto::class.java)
                            startActivity(intent)
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, DelicateCoroutinesApi::class)
@Composable
fun OnBoardingPager(
    item: List<OnBoardingData>,
    pagerState: PagerState,
    onComplete: () -> Unit, // Parámetro para manejar la finalización del onboarding
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(state = pagerState) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(item[page].colorFondo),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Image(
                        modifier = Modifier
                            .size(400.dp)
                            .fillMaxWidth(),
                        painter = painterResource(id = item[page].image),
                        contentDescription = ""
                    )
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ElevatedCard(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = formaCardBotton.large
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IndicadorPagina(items = item, paginaActual = pagerState.currentPage)

                    Text(
                        text = item[pagerState.currentPage].textoPrincipal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, end = 30.dp),
                        color = item[pagerState.currentPage].colorPrincipal,
                        fontFamily = FuentePoppins,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = item[pagerState.currentPage].subTexto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 40.dp, end = 20.dp),
                        color = Color.Gray,
                        fontFamily = FuentePoppins,
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraLight
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Espaciador flexible para ocupar el espacio restante

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (pagerState.currentPage != 2) {
                            TextButton(onClick = {
                                GlobalScope.launch {
                                    pagerState.scrollToPage(
                                        pagerState.currentPage + 2,
                                        pageOffset = 0f
                                    )
                                }
                            }) {
                                Text(
                                    text = "Omitir",
                                    color = Color(0xFF292D32),
                                    fontFamily = FuentePoppins,
                                    textAlign = TextAlign.Right,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    GlobalScope.launch {
                                        pagerState.scrollToPage(
                                            pagerState.currentPage + 1,
                                            pageOffset = 0f
                                        )
                                    }
                                },
                                border = BorderStroke(
                                    14.dp,
                                    item[pagerState.currentPage].colorPrincipal
                                ),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = item[pagerState.currentPage].colorPrincipal
                                ),
                                modifier = Modifier.size(65.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.flechita),
                                    contentDescription = "",
                                    modifier = Modifier.size(70.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    onComplete() // se invoca la función para completar el onboarding
                                },
                                colors = ButtonDefaults.buttonColors(item[pagerState.currentPage].colorPrincipal),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Iniciar",
                                    color = Color.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndicadorPagina(items: List<OnBoardingData>, paginaActual: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(top = 20.dp)
    ) {
        repeat(items.size) {
            Indicador(
                siEsSeleccionado = it == paginaActual,
                color = items[it].colorPrincipal
            )
        }
    }
}

@Composable
fun Indicador(siEsSeleccionado: Boolean, color: Color) {
    val tamaño = if (siEsSeleccionado) 20.dp else 8.dp
    Box(
        modifier = Modifier
            .padding(3.dp)
            .size(tamaño)
            .clip(CircleShape)
            .background(color)
    )
}


