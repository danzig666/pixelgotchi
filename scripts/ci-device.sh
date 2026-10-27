#!/usr/bin/env bash
set -euo pipefail
mkdir -p device-output
collect_evidence() {
  adb logcat -d -t 3000 > device-output/logcat.txt || true
  adb exec-out screencap -p > device-output/final-screen.png || true
  adb pull /data/local/tmp/pixelgotchi-screenshots device-output/ || true
}
trap collect_evidence EXIT
./gradlew connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true --stacktrace
adb pull /data/local/tmp/pixelgotchi-screenshots device-output/ || true
# A real force-stop/start smoke test; offline state is reconciled on launch.
adb shell am force-stop hu.pixelgotchi
adb shell am start -W -n hu.pixelgotchi/.MainActivity > device-output/relaunch.txt
sleep 2
adb exec-out screencap -p > device-output/relaunch.png
# Exercise enlarged system text and a small phone window, then restore the emulator.
adb shell wm size 640x1136
adb shell wm density 320
adb shell settings put system font_scale 2.0
adb shell am force-stop hu.pixelgotchi
adb shell am start -W -n hu.pixelgotchi/.MainActivity > device-output/large-text-launch.txt
sleep 2
adb exec-out screencap -p > device-output/phone-large-text.png
adb shell settings put system font_scale 1.0
adb shell wm size reset
adb shell wm density reset
