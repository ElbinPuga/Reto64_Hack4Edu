package com.elbin.alfabedu

import androidx.compose.ui.graphics.Color
import com.elbin.alfabedu.ui.theme.ColorAzul
import java.security.Principal

data class OnBoardingData(
    val image:Int,
    val colorFondo:Color = ColorAzul,
    val colorPrincipal: Color = ColorAzul,
    val textoPrincipal:String,
    val subTexto:String

)
