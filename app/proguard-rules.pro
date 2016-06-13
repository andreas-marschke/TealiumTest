# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Development\DevResources\SDK\Android_Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Prevent Proguard from optimizing out the beacon sending part of
# the soasta mPulse Native for Mobile implementation
# This has been a reported issue observed in other applications
-keep class com.soasta.** { *; }
-keepattributes Signature

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
