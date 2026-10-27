package hu.pixelgotchi.ui

import hu.pixelgotchi.R
import hu.pixelgotchi.engine.*

fun stageText(stage: Stage) = when(stage) {
    Stage.EGG->R.string.stage_egg;Stage.BABY->R.string.stage_baby;Stage.CHILD->R.string.stage_child
    Stage.TEEN->R.string.stage_teen;Stage.ADULT->R.string.stage_adult;Stage.ELDER->R.string.stage_elder
}
fun causeText(cause: DeathCause?)=when(cause) {
    DeathCause.STARVATION->R.string.cause_starvation;DeathCause.ILLNESS->R.string.cause_illness
    DeathCause.BOTH->R.string.cause_both;else->R.string.cause_age
}
fun stateText(p: Pet)=when {
    !p.alive->R.string.remember;p.stage==Stage.EGG->R.string.state_egg;p.sleeping->R.string.state_sleep
    p.sick->R.string.state_sick;p.food==0->R.string.state_hungry;p.joy==0->R.string.state_sad
    p.poop>0->R.string.state_dirty;p.falseCall->R.string.state_call;!p.light->R.string.state_dark
    p.food<4->R.string.state_wake;else->R.string.state_good
}
fun feedbackText(f: Feedback)=when(f) {
    Feedback.MEAL->R.string.f_meal;Feedback.SNACK->R.string.f_snack;Feedback.FULL->R.string.f_full
    Feedback.CLEAN->R.string.f_clean;Feedback.CURED->R.string.f_cured;Feedback.HEALTHY->R.string.f_healthy
    Feedback.LIGHT->R.string.f_light;Feedback.TAUGHT->R.string.f_taught;Feedback.NO_CALL->R.string.f_no_call
    Feedback.NEED_CARE->R.string.f_need_care;Feedback.SLEEPING->R.string.f_sleep;Feedback.EGG->R.string.f_egg
    Feedback.DEAD->R.string.f_dead;Feedback.CANCELLED->R.string.f_cancelled;Feedback.LEFT->R.string.f_left
    Feedback.RIGHT->R.string.f_right;Feedback.WON->R.string.game_win;Feedback.LOST->R.string.game_lose;Feedback.GAME->R.string.guess_hint
}
val variantTexts=listOf(R.string.variant_0,R.string.variant_1,R.string.variant_2,R.string.variant_3,R.string.variant_4,R.string.variant_5,R.string.variant_6)
