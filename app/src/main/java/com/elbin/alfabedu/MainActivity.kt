package com.elbin.alfabedu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.elbin.alfabedu.ui.theme.AlfabeduTheme
import com.elbin.alfabedu.ui.theme.ColorAmarillo
import com.elbin.alfabedu.ui.theme.ColorAzul
import com.elbin.alfabedu.ui.theme.ColorVerde
import com.elbin.alfabedu.ui.theme.FuenteMontserrat
import com.elbin.alfabedu.ui.theme.FuentePoppins
import com.elbin.alfabedu.ui.theme.SubirTomarFoto
import com.elbin.alfabedu.ui.theme.formaCardBotton
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlfabeduTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val items = ArrayList<OnBoardingData>()

                    items.add(
                        OnBoardingData(
                            R.drawable.pantalla1,
                            colorFondo = Color(0xFF008AC3),
                            colorPrincipal = Color(0xFF00B5EA),
                            textoPrincipal = "Aprende",
                            subTexto = "¡Diviértete con las letras!"
                        )
                    )

                    items.add(
                        OnBoardingData(
                            R.drawable.nino_escribiendo_yellowbg,
                            colorFondo = Color(0xFFFAD206),
                            colorPrincipal = ColorAmarillo,
                            textoPrincipal = "Muestra tus dibujos",
                            subTexto = "Toma fotos de tus letras"
                        )
                    )

                    items.add(
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingPager(
    item: List<OnBoardingData>,
    pagerState: PagerState,
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
                                        pagerState.currentPage+2,
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
                                            pagerState.currentPage+1,
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
                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    val intent = Intent(context,SubirTomarFoto::class.java)
                                    context.startActivity(intent)
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
    val ancho = animateDpAsState(targetValue = if (siEsSeleccionado) 40.dp else 10.dp)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .height(10.dp)
            .width(ancho.value)
            .clip(CircleShape)
            .background(
                if (siEsSeleccionado) color else Color.Gray.copy(alpha = 0.5f)
            )
    )
}
