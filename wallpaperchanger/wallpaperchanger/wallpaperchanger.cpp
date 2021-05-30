#include "pch.h"
#include <iostream>  
#include <windows.h>  
#include <fstream> 
#include <cstdlib> 
#include <jni.h>
#include "wallpaperchanger.h"  


JNIEXPORT jint JNICALL Java_WallpaperChanger_putImage
(JNIEnv* env, jclass, jstring wallpaper) {
	const wchar_t* wallpaper_file = (wchar_t*)env->GetStringChars(wallpaper, NULL);
	int return_value = SystemParametersInfo(SPI_SETDESKWALLPAPER, 0, (void*)wallpaper_file, SPIF_UPDATEINIFILE | SPIF_SENDCHANGE);
	env->ReleaseStringChars(wallpaper, (jchar*)wallpaper_file);
	return 0;
}