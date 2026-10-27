package hu.pixelgotchi.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import hu.pixelgotchi.R
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.delay
import kotlin.math.floor

val Ink=Color(0xff26372e)
val Lcd=Color(0xffc8d4ac)
private val egg=listOf("00000111100000","00001111110000","00011011111000","00111111101100","00110111111100","01111101111110","01111111101110","01101111111110","00111101111100","00011111111000","00001111110000")
private val baby=listOf("00001111110000","00111111111100","01111111111110","01100111100110","01100111100110","01111111111110","00111100111100","00011111111000","00110000001100")
private val child=listOf("00110000001100","00111111111100","01111111111110","11100111100111","11100111100111","11111111111111","01111000011110","00111111111100","00111000011100","01100000000110")
private val teen=listOf("00000111100000","00011111111000","00111111111100","01111111111110","01100111100110","11100111100111","11111111111111","01111000011110","01111111111110","00111111111100","01110000001110")
// Original, deliberately low-resolution silhouettes. No external sprite or font requests at runtime.
private val adults=listOf(
    listOf("01000000000010","01100000000110","01110011001110","00111111111100","01111111111110","11100111100111","11100111100111","11111111111111","01111000011110","00111111111100","01110011001110","11000000000011"),
    listOf("00110000001100","01110000001110","01110000001110","01111111111110","01111111111110","11100111100111","11100111100111","11111111111111","01111000011110","00111111111100","00111000011100","01111000011110"),
    listOf("01100000000110","01111000011110","01111111111110","11111111111111","11100111100111","11100111100111","11111111111111","01111011011110","01111100111110","00111111111100","00111000011100","01100000000110"),
    listOf("00001111110000","00111111111100","01111111111110","11111111111111","11100111100111","11100111100111","11111111111111","11111000011111","01111111111110","00111111111100","01110000001110"),
    listOf("00000111100000","00011111111000","00111111111100","01111111111110","11111111111111","11001111110011","11001111110011","11111111111111","11111000011111","01111111111110","00111111111100","01110000001110"),
    listOf("00000110000000","00001111100000","00000011000000","00111111111100","01111111111110","11100111100111","11100111100111","11111111111111","01111000011110","00111111111100","01110000001110"),
    listOf("00011000011000","00111100111100","01111111111110","11111111111111","11100111100111","11100111100111","11111111111111","11111000011111","01111111111110","00111111111100","01100011000110")
)
private val grave=listOf("00011111111000","00111111111100","01111111111110","01111100111110","01110000001110","01111100111110","01111100111110","01111111111110","01111111111110","11111111111111")
private val waste=listOf("00100","01110","11111","01110","11111")
private val food=listOf("011110","111111","110011","111111","011110")
private val heart=listOf("0110110","1111111","1111111","0111110","0011100","0001000")
private fun DrawScope.pixels(pattern: List<String>, x: Float,y: Float,pixel: Float,color: Color=Ink,blink: Boolean=false) {
    pattern.forEachIndexed { row,line -> line.forEachIndexed { col,c ->
        if(c=='1' && !(blink && (row==5 || row==4) && (col in 3..4 || col in 9..10)))
            drawRect(color,Offset(x+col*pixel,y+row*pixel),Size(pixel,pixel))
    } }
}
@Composable fun PetCanvas(p: Pet?,modifier: Modifier=Modifier,animate: Boolean=false,feedback: Feedback?=null) {
    var frame by remember { mutableIntStateOf(0) }
    val owner=LocalLifecycleOwner.current
    LaunchedEffect(animate,owner) {
        if(animate)owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { while(true) { delay(if(p?.stage==Stage.ELDER)800 else 400);frame++ } }
    }
    var previousStage by remember(p?.id) { mutableStateOf(p?.stage) }
    var evolving by remember(p?.id) { mutableStateOf(false) }
    LaunchedEffect(p?.stage) {
        if(animate && previousStage!=p?.stage) { evolving=true;delay(2400);evolving=false }
        previousStage=p?.stage
    }
    val description=stringResource(R.string.lcd_description,p?.name ?: stringResource(R.string.stage_egg))
    Canvas(modifier.semantics { contentDescription=description }) {
        val px=floor(minOf(size.width/72,size.height/29)).coerceAtLeast(1f)
        val width=72*px;val height=29*px;val ox=floor((size.width-width)/2);val oy=floor((size.height-height)/2)
        for(x in 0..71)for(y in 0..28)drawRect(Ink.copy(alpha=.045f),Offset(ox+x*px,oy+y*px),Size(px*.8f,px*.8f))
        val pattern=when {
            p==null || p.stage==Stage.EGG -> egg
            !p.alive -> grave
            p.stage==Stage.BABY -> baby
            p.stage==Stage.CHILD -> child
            p.stage==Stage.TEEN -> teen
            else -> adults[p.variant.coerceIn(0,6)]
        }
        val motion=if(p!=null && p.alive && !p.sleeping && p.stage!=Stage.EGG)listOf(-3,0,3,0)[frame%4] else 0
        val eating=feedback==Feedback.MEAL || feedback==Feedback.SNACK
        val joyful=feedback==Feedback.WON || feedback==Feedback.TAUGHT
        val bounce=if(joyful && frame%2==0)-2 else 0
        val x=ox+(29+motion)*px;val y=oy+(8+bounce)*px
        val direction=p?.game?.lastDirection ?: -1
        val side=listOf("00001111110000","00111111111100","01111111111110","10011111111111","10011111111111","11111111111111","10001111111110","01111111111100","00111001110000","01100000110000")
        val shown=if(evolving && frame%2==0)egg else if(direction>=0 && p?.alive==true)side else pattern
        if(direction==1 && !evolving)withTransform({translate(x+14*px,y);scale(-1f,1f,pivot=Offset.Zero)}) {
            pixels(shown,0f,0f,px,blink=p?.sleeping==true || frame%9==8)
        } else pixels(shown,x,y,px,blink=p?.sleeping==true || frame%9==8)
        if(evolving) {
            pixels(listOf("010","111","010"),ox+19*px,oy+(4+frame%2)*px,px)
            pixels(listOf("010","111","010"),ox+49*px,oy+(15-frame%2)*px,px)
        }
        if(p!=null) {
            repeat(minOf(p.poop,4)) { i ->pixels(waste,ox+(4+i*9)*px,oy+23*px,px) }
            if(p.sick && p.alive) {
                drawRect(Ink,Offset(ox+58*px,oy+5*px),Size(3*px,9*px))
                drawRect(Ink,Offset(ox+55*px,oy+8*px),Size(9*px,3*px))
            }
            if(p.sleeping && p.alive) { pixels(listOf("1111","0010","0100","1111"),ox+48*px,oy+4*px,px) }
            if(p.stage==Stage.ELDER && p.alive)drawRect(Ink,Offset(x+15*px,y+6*px),Size(px,8*px))
            if(eating && frame%2==0 && p.alive)pixels(food,x+17*px,y+6*px,px)
            if(joyful && p.alive)pixels(heart,x+16*px,y-3*px,px)
            if(feedback==Feedback.FULL && p.alive)pixels(listOf("111","101"),x+5*px,y+7*px,px,Lcd)
            if(!p.alive && frame%2==0)pixels(listOf("010","111","010"),x+18*px,y-4*px,px)
        }
        drawRect(Ink.copy(alpha=.4f),Offset(ox+3*px,oy+28*px),Size(66*px,px))
    }
}
private val icons=listOf(
    listOf("001100","011110","111111","111111","011110","001100"),
    listOf("001100","111111","101101","111111","010010","100001"),
    listOf("000010","000100","001100","011110","111111","100001"),
    listOf("001100","001100","111111","111111","001100","001100"),
    listOf("001100","011110","011110","001100","001100","011110"),
    listOf("111111","101101","101101","111111","111111","000000"),
    listOf("001100","001100","000000","001100","001100","001100")
)
@Composable fun PixelIcon(index: Int,modifier: Modifier=Modifier,color: Color=Ink) { Canvas(modifier) {
    val px=floor(minOf(size.width,size.height)/6).coerceAtLeast(1f)
    pixels(icons[index],floor((size.width-6*px)/2),floor((size.height-6*px)/2),px,color)
} }
