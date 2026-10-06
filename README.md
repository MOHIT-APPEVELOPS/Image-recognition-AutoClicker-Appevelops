# Advanced Auto Clicker

Android Studio project for Android 11+.

## Included
- AccessibilityService
- Gesture-based automatic clicking
- Screenshot capture through AccessibilityService.takeScreenshot()
- Lightweight template/image matching
- Template image picker
- Accessibility click-event macro recording
- Macro playback
- Floating STOP overlay service
- Material 3 UI

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Build > Build APK(s).
4. Install the debug APK.
5. Open the app.
6. Enable the app under Android Settings > Accessibility.
7. Grant "Display over other apps" if you want the floating control.

## Image recognition
Select a small, distinctive PNG/JPG template. Start recognition and set a threshold such as 0.90.

The matcher is intentionally dependency-free. It samples pixels to reduce CPU load. For production-grade CV, replace TemplateMatcher with OpenCV or another optimized matcher.

## Macro recorder limitation
Android's standard. AccessibilityService receives accessibility events, not a raw global touch stream. This implementation records view-click events and their screen bounds, then replays them as accessibility gestures.

If you need true raw touch/swipe recording across arbitrary apps, you need a different architecture (for example, an approved input/accessibility approach appropriate to your target device/Android version).

## Important
Automation behavior must comply with the target app's rules and Android/Google Play policies. Accessibility APIs are intended for accessibility and user-authorized automation; do not use this project to bypass security controls, CAPTCHAs, anti-bot protections, or access controls.
