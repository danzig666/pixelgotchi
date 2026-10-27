package hu.pixelgotchi.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import hu.pixelgotchi.GameViewModel
import hu.pixelgotchi.R
import hu.pixelgotchi.data.*
import hu.pixelgotchi.engine.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Cream=Color(0xfffaf7ef)
private val Purple=Color(0xff685087)
private val Shells=listOf(Color(0xffc0a7e4),Color(0xffefb79f),Color(0xffacd3bc),Color(0xffa6cce3))
private val Labels=listOf(R.string.food,R.string.play,R.string.clean,R.string.medicine,R.string.lamp,R.string.teach,R.string.status)
private val eggShape=object: Shape {
    override fun createOutline(size: Size,layoutDirection: LayoutDirection,density: Density): Outline {
        val w=size.width;val h=size.height
        return Outline.Generic(Path().apply { moveTo(w*.5f,0f);cubicTo(w,0f,w,h*.1f,w,h*.45f);cubicTo(w,h*.87f,w*.83f,h,w*.5f,h);cubicTo(w*.17f,h,0f,h*.87f,0f,h*.45f);cubicTo(0f,h*.1f,0f,0f,w*.5f,0f);close() })
    }
}
@Composable private fun word(id: Int)=stringResource(id)
@Composable private fun ageText(p: Pet)=if(p.age()<GameBalance.DAY)stringResource(R.string.age_hours,p.age()/GameBalance.HOUR) else stringResource(R.string.age_days,p.age()/GameBalance.DAY)
@Composable private fun Heading(text: String)=Text(text,fontSize=25.sp,fontWeight=FontWeight.Bold,color=Ink)
@Composable private fun ActionButton(id: Int,onClick:()->Unit,modifier: Modifier=Modifier,enabled: Boolean=true) {
    Button(onClick=onClick,enabled=enabled,modifier=modifier.heightIn(min=56.dp),shape=RoundedCornerShape(18.dp)) { Text(word(id),textAlign=TextAlign.Center) }
}
@Composable fun PixelgotchiApp(vm: GameViewModel,destination: Pair<String?,String?>) {
    val world by vm.world.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val feedback by vm.feedback.collectAsStateWithLifecycle()
    var profile by rememberSaveable { mutableStateOf<String?>(null) }
    var modal by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var tip by rememberSaveable { mutableStateOf(false) }
    val context=LocalContext.current
    val effects=remember { Effects(context) }
    DisposableEffect(effects) { onDispose { effects.close() } }
    val lifecycle=LocalLifecycleOwner.current
    LaunchedEffect(lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while(true) {
                vm.tick()
                val w=vm.world.value
                val now=w?.clock?.logical ?: 0L
                val next=w?.profiles?.values?.filterNotNull()?.minOfOrNull { Simulator().nextEventAt(it) } ?: Long.MAX_VALUE
                delay(if(next==Long.MAX_VALUE)60_000L else (next-now).coerceIn(1L,60_000L))
            }
        }
    }
    LaunchedEffect(destination,world!=null) {
        val p=world?.profiles?.get(destination.first)
        if(p!=null && p.id==destination.second)profile=destination.first
    }
    val p=world?.profiles?.get(profile)
    var seenPet by remember { mutableStateOf<String?>(null) }
    var seenStage by remember { mutableStateOf<Stage?>(null) }
    var seenAlive by remember { mutableStateOf(true) }
    LaunchedEffect(p?.id,p?.stage,p?.alive) {
        if(p!=null && seenPet==p.id && (seenStage!=p.stage || seenAlive!=p.alive))effects.play(world!!.preferences,if(p.alive)3 else 4)
        seenPet=p?.id;seenStage=p?.stage;seenAlive=p?.alive ?: true
    }
    LaunchedEffect(feedback) { if(feedback!=null) {
        if(feedback in listOf(Feedback.WON,Feedback.MEAL,Feedback.CURED,Feedback.TAUGHT))world?.let{effects.play(it.preferences,1)}
        delay(2600);vm.feedback.value=null
    } }
    val requestNotifications=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> vm.preferences { it.notifications=granted } }
    fun click() { world?.let { effects.play(it.preferences) } }
    fun perform(index: Int) {
        val pet=p ?: return
        click();selected=index
        when(index) {
            0->modal="food"
            1->vm.act(pet,Action.START_GAME)
            2->vm.act(pet,Action.CLEAN)
            3->vm.act(pet,Action.MEDICINE)
            4->vm.act(pet,Action.LIGHT)
            5->vm.act(pet,Action.TEACH)
            6->modal="status"
        }
    }
    BackHandler(profile!=null || modal.isNotEmpty() || p?.game!=null) {
        if(modal.isNotEmpty())modal="" else if(p?.game!=null)vm.act(p,Action.CANCEL_GAME) else { profile=null;vm.feedback.value=null }
    }
    val fonts=FontFamily(Font(R.font.pixel_sans))
    val typography=Typography().let { base -> base.copy(bodyLarge=base.bodyLarge.copy(fontFamily=fonts),bodyMedium=base.bodyMedium.copy(fontFamily=fonts),labelLarge=base.labelLarge.copy(fontFamily=fonts),titleLarge=base.titleLarge.copy(fontFamily=fonts)) }
    MaterialTheme(colorScheme=lightColorScheme(primary=Purple,onPrimary=Color.White,background=Cream,surface=Cream,onSurface=Ink,secondary=Ink),typography=typography) {
        Surface(Modifier.fillMaxSize(),color=Cream) {
            if(error) Column(Modifier.safeDrawingPadding().padding(28.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Heading(word(R.string.error_title));Text(word(R.string.save_error));ActionButton(R.string.retry,{vm.refresh()})
            } else if(world==null) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { Text(word(R.string.loading)) }
            else Column(Modifier.safeDrawingPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically) {
                    TextButton(onClick={if(profile!=null){profile=null;vm.feedback.value=null}else modal="help"},modifier=Modifier.heightIn(min=48.dp)) { Text(word(if(profile!=null)R.string.select_profile else R.string.brand),fontWeight=FontWeight.Bold,letterSpacing=2.sp) }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick={modal="settings"},modifier=Modifier.heightIn(min=48.dp)) { Text(word(R.string.settings)) }
                }
                if(profile==null) Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.widthIn(max=650.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text(word(R.string.subtitle),fontSize=13.sp,color=Purple,fontWeight=FontWeight.Bold)
                        Text(word(R.string.title),fontSize=34.sp,lineHeight=43.sp,fontWeight=FontWeight.Bold,color=Ink)
                        Text(word(R.string.profile_hint),color=Ink.copy(alpha=.7f))
                    }
                    Spacer(Modifier.height(6.dp))
                    world!!.profiles.forEach { (name,pet) ->
                        ProfileCard(name,pet,world!!.preferences.colors[name] ?: 0,onClick={profile=name;selected=0;vm.feedback.value=null;if(pet==null)modal="name";click()})
                    }
                    Text(word(R.string.clock_note),fontSize=12.sp,textAlign=TextAlign.Center,color=Ink.copy(alpha=.65f))
                    TextButton(onClick={modal="help"}) { Text(word(R.string.help)) }
                } else if(p==null) Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)) {
                    Heading(stringResource(R.string.profile_of,profile!!));PetCanvas(null,Modifier.fillMaxWidth().height(180.dp));Text(word(R.string.new_friend));ActionButton(R.string.start,{modal="name"})
                } else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val wide=maxWidth>=800.dp
                    if(wide) Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),horizontalArrangement=Arrangement.spacedBy(28.dp),verticalAlignment=Alignment.CenterVertically) {
                        Device(p,world!!.preferences.colors[profile] ?: 0,selected,feedback,Modifier.weight(1f),busy,::perform,{selected=(selected+1)%7;click()},{if(!busy)perform(selected)},{if(p.game!=null)vm.act(p,Action.CANCEL_GAME) else {profile=null;vm.feedback.value=null}}, {modal="attention"})
                        Column(Modifier.weight(.65f),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                            Heading(p.name);Stats(p)
                            if(!p.alive)DeathDetails(p,{modal="restart"})
                            ActionButton(R.string.album,{vm.album(profile!!);modal="album"})
                            Text(word(R.string.life_warning),fontSize=12.sp)
                        }
                    } else Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=12.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        Device(p,world!!.preferences.colors[profile] ?: 0,selected,feedback,Modifier.widthIn(max=480.dp).fillMaxWidth(),busy,::perform,{selected=(selected+1)%7;click()},{if(!busy)perform(selected)},{if(p.game!=null)vm.act(p,Action.CANCEL_GAME) else {profile=null;vm.feedback.value=null}},{modal="attention"})
                        if(!p.alive)DeathDetails(p,{modal="restart"})
                        TextButton(onClick={vm.album(profile!!);modal="album"},modifier=Modifier.heightIn(min=48.dp)) { Text(word(R.string.album)) }
                    }
                }
            }
            if(!error && world!=null) {
                if(p?.game!=null && p.alive && modal.isEmpty())GameDialog(p,busy,{vm.guess(p,it);click()},{vm.act(p,Action.CANCEL_GAME)},{vm.act(p,Action.START_GAME)})
                when(modal) {
                    "name"->NameDialog(profile ?: "Lili",busy,{modal=""}) { name -> vm.create(profile ?: "Lili",name);modal=if(world!!.preferences.introduced)"" else "intro" }
                    "intro","help"->MessageDialog(R.string.intro_title,word(R.string.intro)+"\n\n"+word(R.string.life_warning),{modal="";vm.preferences{it.introduced=true}})
                    "food"->AppDialog(word(R.string.food),{modal=""}) {
                        Text(word(R.string.food_hint));ActionButton(R.string.meal,{if(p!=null){vm.act(p,Action.MEAL);click()};modal=""},Modifier.fillMaxWidth(),!busy)
                        ActionButton(R.string.snack,{if(p!=null){vm.act(p,Action.SNACK);click()};modal=""},Modifier.fillMaxWidth(),!busy)
                    }
                    "status"->AppDialog(word(R.string.status),{modal=""}) { if(p!=null)Stats(p) }
                    "attention"->MessageDialog(R.string.attention,if(p!=null)word(stateText(p))+"\n\n"+word(R.string.attention_hint) else word(R.string.attention_none),{modal=""})
                    "restart"->ConfirmDialog(R.string.restart_title,R.string.restart_body,R.string.new_egg,{modal=""}) { modal="name" }
                    "album"->{val album by vm.album.collectAsStateWithLifecycle();AppDialog(stringResource(R.string.album_of,profile ?: "Lili"),{modal=""}) {
                        if(album.isEmpty())Text(word(R.string.empty_album))
                        album.forEach { old -> Card(colors=CardDefaults.cardColors(containerColor=Lcd.copy(alpha=.5f))) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text(old.name,fontWeight=FontWeight.Bold);PetCanvas(old,Modifier.fillMaxWidth().height(110.dp));Text(ageText(old));Text(word(causeText(old.deathCause)));Dates(old)
                        } } }
                    }}
                    "settings"->SettingsDialog(world!!.preferences,profile ?: "Lili",{modal=""},{change->vm.preferences(change)},{modal="notify"},{tip=true},{modal="parents"})
                    "notify"->ConfirmDialog(R.string.notifications,R.string.notification_question,R.string.enable,{modal="settings"}) {
                        if(Build.VERSION.SDK_INT>=33)requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS) else vm.preferences{it.notifications=true};modal="settings"
                    }
                    "parents"->ParentDialog(p,{modal="settings"},{schedule->if(profile!=null)vm.schedule(profile!!,schedule)},{modal="erase"})
                    "erase"->ConfirmDialog(R.string.erase_title,R.string.erase_body,R.string.erase_confirm,{modal="parents"}) { vm.erase();profile=null;modal="" }
                }
                if(tip)MessageDialog(R.string.parents,word(R.string.hold_parent),{tip=false})
            }
        }
    }
}
@Composable private fun ProfileCard(name: String,p: Pet?,color: Int,onClick:()->Unit) {
    val description=stringResource(R.string.profile_description,name)
    Card(onClick=onClick,modifier=Modifier.widthIn(max=650.dp).fillMaxWidth().semantics{contentDescription=description},shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Shells[color].copy(alpha=.24f))) {
        Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(100.dp,112.dp).background(Brush.verticalGradient(listOf(Shells[color].copy(alpha=.65f),Shells[color])),eggShape).padding(10.dp),contentAlignment=Alignment.Center) {
                PetCanvas(p,Modifier.fillMaxWidth().height(65.dp).clip(RoundedCornerShape(8.dp)).background(Lcd))
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                Text(name,fontSize=28.sp,fontWeight=FontWeight.Bold,color=Ink)
                Text(p?.name ?: word(R.string.not_started),fontWeight=FontWeight.Medium)
                Text(if(p==null)word(R.string.new_friend) else if(!p.alive)stringResource(R.string.dead_name,p.name) else word(stateText(p)),fontSize=12.sp,color=Ink.copy(alpha=.75f))
            }
        }
    }
}
@Composable private fun Device(p: Pet,color: Int,selected: Int,feedback: Feedback?,modifier: Modifier,busy: Boolean,perform:(Int)->Unit,choose:()->Unit,ok:()->Unit,back:()->Unit,attention:()->Unit) {
    val shell=Shells[color]
    Column(modifier.background(Brush.linearGradient(listOf(shell.copy(alpha=.55f),shell,shell.copy(red=(shell.red*.85f)))),eggShape)
        .border(2.dp,shell.copy(red=shell.red*.8f,green=shell.green*.8f,blue=shell.blue*.8f),eggShape)
        .padding(horizontal=34.dp).padding(top=40.dp,bottom=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(p.profile+" · "+word(R.string.brand),fontSize=12.sp,letterSpacing=2.sp,fontWeight=FontWeight.Bold,color=Ink.copy(alpha=.7f))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Ink.copy(alpha=.15f)).padding(4.dp).clip(RoundedCornerShape(14.dp)).background(if(p.light)Lcd else Color(0xffa6b68e)).border(2.dp,Ink.copy(alpha=.5f),RoundedCornerShape(14.dp)).padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Text(p.name,fontSize=14.sp,lineHeight=20.sp,fontWeight=FontWeight.Bold,color=Ink,modifier=Modifier.weight(1f))
                Text(word(stageText(p.stage)),fontSize=11.sp,color=Ink)
            }
            PetCanvas(p,Modifier.fillMaxWidth().aspectRatio(2.8f),animate=true,feedback=feedback)
            Hearts(R.string.hearts_food,p.food);Hearts(R.string.hearts_joy,p.joy)
            Text(if(!p.alive)stringResource(R.string.dead_name,p.name) else if(feedback!=null)word(feedbackText(feedback)) else word(stateText(p)),fontSize=12.sp,lineHeight=18.sp,color=Ink,modifier=Modifier.fillMaxWidth().heightIn(min=32.dp),textAlign=TextAlign.Center)
        }
        for(row in listOf(0..3,4..6)) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(2.dp)) {
            row.forEach { index ->
                val label=word(Labels[index]);val selectedDescription=stringResource(R.string.selected_action,label)
                Column(Modifier.weight(1f).heightIn(min=56.dp).clip(RoundedCornerShape(12.dp))
                    .background(if(index==selected)Color.White.copy(alpha=.55f) else Color.Transparent)
                    .clickable(enabled=!busy && (p.alive || index==6)) { perform(index) }
                    .semantics { contentDescription=if(index==selected)selectedDescription else label;this.selected=index==selected }
                    .padding(vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    PixelIcon(index,Modifier.size(20.dp));Text(label,fontSize=10.sp,textAlign=TextAlign.Center,lineHeight=14.sp,color=Ink)
                }
            }
        }
        OutlinedButton(onClick=attention,modifier=Modifier.heightIn(min=48.dp),border=BorderStroke(1.dp,Ink.copy(alpha=.35f)),contentPadding=PaddingValues(horizontal=14.dp,vertical=4.dp)) {
            Text((if(p.severity()>0)"● " else "○ ")+word(R.string.attention),color=Ink,fontSize=12.sp)
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            listOf(R.string.choose to choose,R.string.ok to ok,R.string.back to back).forEach { (label,callback) ->
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    val description=word(label)
                    Box(Modifier.sizeIn(minWidth=56.dp,minHeight=56.dp).size(58.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xfffbf4d8),Color(0xffdbca9e))))
                        .border(2.dp,Ink.copy(alpha=.22f),CircleShape).clickable(enabled=!busy) { callback() }.semantics { contentDescription=description;role=Role.Button },contentAlignment=Alignment.Center) {
                        Box(Modifier.size(9.dp).background(Ink.copy(alpha=.5f),CircleShape))
                    }
                    Text(description,fontSize=10.sp,textAlign=TextAlign.Center,color=Ink)
                }
            }
        }
    }
}
@Composable private fun Hearts(label: Int,count: Int) {
    val description=stringResource(R.string.hearts_description,word(label),count)
    Row(Modifier.fillMaxWidth().semantics(mergeDescendants=true){contentDescription=description},horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
        Text(word(label),fontSize=11.sp,color=Ink);Text("♥".repeat(count)+"♡".repeat(4-count),fontSize=18.sp,lineHeight=22.sp,letterSpacing=1.sp,color=Ink)
    }
}
@Composable private fun Stats(p: Pet) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(word(stageText(p.stage))+" · "+ageText(p),fontWeight=FontWeight.Bold)
        if(p.stage>=Stage.ADULT)Text(word(variantTexts[p.variant.coerceIn(0,6)]))
        Hearts(R.string.hearts_food,p.food);Hearts(R.string.hearts_joy,p.joy)
        Text(stringResource(R.string.health,p.health.toInt()));Text(stringResource(R.string.weight,p.weight));Text(stringResource(R.string.training,p.training));Text(stringResource(R.string.games_won,p.wins));Text(stringResource(R.string.care_errors,p.mistakes))
        Text(word(if(p.light)R.string.light_on else R.string.light_off))
        Text(stringResource(R.string.sleep_schedule,formatMinute(p.schedule.bedtime),formatMinute(p.schedule.wake)))
        if(p.pendingSchedule!=null)Text(word(R.string.schedule_pending))
        if(p.history.isNotEmpty()) {
            Text(word(R.string.history),fontWeight=FontWeight.Bold)
            p.history.forEach { e -> Text(word(stageText(e.stage))+" · "+dateText(e.at,p.schedule.zone),fontSize=12.sp) }
        }
    }
}
private fun formatMinute(m: Int)="%02d:%02d".format(java.util.Locale.ROOT,m/60,m%60)
private fun dateText(at: Long,zone: String)=DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm",java.util.Locale.forLanguageTag("hu")).format(Instant.ofEpochMilli(at).atZone(ZoneId.of(zone)))
@Composable private fun Dates(p: Pet) {
    if(p.bornAt>0)Text(stringResource(R.string.born,dateText(p.bornAt,p.schedule.zone)),fontSize=12.sp)
    if(p.diedAt>0)Text(stringResource(R.string.died,dateText(p.diedAt,p.schedule.zone)),fontSize=12.sp)
}
@Composable private fun DeathDetails(p: Pet,restart:()->Unit) {
    Column(Modifier.widthIn(max=480.dp).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Heading(stringResource(R.string.dead_name,p.name));Text(word(causeText(p.deathCause)));Text(ageText(p));Dates(p);ActionButton(R.string.new_egg,restart)
    }
}

@Composable private fun AppDialog(title: String,dismiss:()->Unit,content: @Composable ColumnScope.()->Unit) {
    AlertDialog(onDismissRequest=dismiss,title={Text(title)},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp),content=content)},confirmButton={TextButton(onClick=dismiss,modifier=Modifier.heightIn(min=48.dp)){Text(word(R.string.close))}})
}
@Composable private fun MessageDialog(title: Int,message: String,dismiss:()->Unit) {
    AlertDialog(onDismissRequest=dismiss,title={Text(word(title))},text={Text(message)},confirmButton={TextButton(onClick=dismiss){Text(word(R.string.understood))}})
}
@Composable private fun ConfirmDialog(title: Int,body: Int,confirm: Int,dismiss:()->Unit,action:()->Unit) {
    AlertDialog(onDismissRequest=dismiss,title={Text(word(title))},text={Text(word(body))},confirmButton={TextButton(onClick=action,modifier=Modifier.heightIn(min=48.dp)){Text(word(confirm))}},dismissButton={TextButton(onClick=dismiss,modifier=Modifier.heightIn(min=48.dp)){Text(word(R.string.cancel))}})
}
@Composable private fun NameDialog(profile: String,busy: Boolean,dismiss:()->Unit,create:(String)->Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest=dismiss,title={Text(word(R.string.name_title))},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(profile,fontWeight=FontWeight.Bold)
        OutlinedTextField(value=name,onValueChange={name=it.take(24)},singleLine=true,label={Text(word(R.string.pet_name))})
        listOf(R.string.name_potyi,R.string.name_morzsi,R.string.name_bogyo).forEach { id -> val suggestion=word(id);OutlinedButton(onClick={name=suggestion},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(suggestion)} }
        Text(word(R.string.life_warning),fontSize=13.sp)
    }},confirmButton={TextButton(onClick={create(name)},enabled=name.trim().isNotEmpty() && !busy,modifier=Modifier.heightIn(min=48.dp)){Text(word(R.string.start))}},dismissButton={TextButton(onClick=dismiss){Text(word(R.string.cancel))}})
}
@Composable private fun GameDialog(p: Pet,busy: Boolean,guess:(Int)->Unit,dismiss:()->Unit,restart:()->Unit) {
    val game=p.game ?: return
    AppDialog(word(R.string.guess_hint),dismiss) {
        PetCanvas(p,Modifier.fillMaxWidth().height(120.dp),animate=true)
        Text(stringResource(R.string.round,game.round,game.hits))
        Text(word(R.string.five_rounds),fontSize=13.sp)
        if(game.lastDirection>=0)Text(word(if(game.lastDirection==0)R.string.f_left else R.string.f_right))
        if(game.finished) {
            Text(word(if(game.hits>=3)R.string.game_win else R.string.game_lose),fontWeight=FontWeight.Bold)
            if(game.rewarded)Text(word(R.string.game_reward))
            ActionButton(R.string.next_game,restart,Modifier.fillMaxWidth(),!busy)
        } else Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            Button(onClick={guess(0)},enabled=!busy,modifier=Modifier.weight(1f).heightIn(min=72.dp)) { Text("← "+word(R.string.left)) }
            Button(onClick={guess(1)},enabled=!busy,modifier=Modifier.weight(1f).heightIn(min=72.dp)) { Text(word(R.string.right)+" →") }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun SettingsDialog(prefs: Preferences,profile: String,dismiss:()->Unit,change:((Preferences)->Unit)->Unit,notify:()->Unit,shortParent:()->Unit,longParent:()->Unit) {
    AppDialog(word(R.string.settings),dismiss) {
        listOf(Triple(R.string.sound,prefs.sound,0),Triple(R.string.vibration,prefs.vibration,1),Triple(R.string.notifications,prefs.notifications,2)).forEach { (id,checked,key) ->
            Row(Modifier.fillMaxWidth().heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically) {
                val switchLabel=word(id)
                Text(switchLabel,Modifier.weight(1f));Switch(checked=checked,onCheckedChange={v->when(key){0->change{it.sound=v};1->change{it.vibration=v};else->if(v)notify() else change{it.notifications=false}}},modifier=Modifier.semantics { contentDescription=switchLabel })
            }
        }
        Text(word(R.string.notification_explain),fontSize=12.sp)
        Text(word(R.string.shell_color)+" · "+profile,fontWeight=FontWeight.Bold)
        val colors=listOf(R.string.color_0,R.string.color_1,R.string.color_2,R.string.color_3)
        colors.forEachIndexed { index,label ->
            val description=word(label)
            Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clip(RoundedCornerShape(12.dp)).background(Shells[index].copy(alpha=.3f)).clickable{change{it.colors[profile]=index}}.padding(12.dp).semantics{contentDescription=description;selected=prefs.colors[profile]==index},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(26.dp).background(Shells[index],CircleShape));Text(description,Modifier.weight(1f));if(prefs.colors[profile]==index)Text("●")
            }
        }
        val parentLabel=word(R.string.parents)
        Box(Modifier.fillMaxWidth().heightIn(min=56.dp).clip(RoundedCornerShape(16.dp)).background(Purple.copy(alpha=.1f)).combinedClickable(onClick=shortParent,onLongClick=longParent,onLongClickLabel=parentLabel).padding(16.dp).semantics{contentDescription=parentLabel},contentAlignment=Alignment.Center) { Text(parentLabel) }
        Text(word(R.string.parent_hint),fontSize=12.sp);Text(word(R.string.privacy),fontSize=12.sp)
    }
}
@Composable private fun ParentDialog(p: Pet?,dismiss:()->Unit,save:(Schedule)->Unit,erase:()->Unit) {
    var bedtime by rememberSaveable { mutableStateOf(formatMinute(p?.schedule?.bedtime ?: 1230)) }
    var wake by rememberSaveable { mutableStateOf(formatMinute(p?.schedule?.wake ?: 420)) }
    var zone by rememberSaveable { mutableStateOf(p?.schedule?.zone ?: ZoneId.systemDefault().id) }
    var validation by rememberSaveable { mutableIntStateOf(0) }
    fun minutes(text: String): Int {
        val parts=text.split(":");require(parts.size==2)
        val h=parts[0].toInt();val m=parts[1].toInt();require(h in 0..23 && m in 0..59);return h*60+m
    }
    AppDialog(word(R.string.parents),dismiss) {
        Text(word(R.string.schedule_hint))
        if(p==null)Text(word(R.string.schedule_no_pet)) else {
            OutlinedTextField(value=bedtime,onValueChange={bedtime=it},label={Text(word(R.string.bedtime))},singleLine=true)
            OutlinedTextField(value=wake,onValueChange={wake=it},label={Text(word(R.string.waketime))},singleLine=true)
            OutlinedTextField(value=zone,onValueChange={zone=it},label={Text(word(R.string.timezone))},singleLine=true)
            if(validation!=0)Text(word(validation))
            ActionButton(R.string.save,{
                try {
                    ZoneId.of(zone);val schedule=Schedule(minutes(bedtime),minutes(wake),zone);require(schedule.valid());save(schedule);validation=R.string.schedule_saved
                } catch(_: Exception) { validation=R.string.invalid_schedule }
            },Modifier.fillMaxWidth())
        }
        HorizontalDivider();OutlinedButton(onClick=erase,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)){Text(word(R.string.erase))}
    }
}
