

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <jni.h>  
#include <string.h>  
#include <android/log.h>  
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/sensor.h>
#include <android_native_app_glue.h>
#include <errno.h>
#include <EGL/egl.h>
//#include <GLES/gl.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/time.h> 
#include <cstddef>
#include <unistd.h>
#include <wctype.h>
#include <sys/types.h>
#include <limits.h>
#include <vector>

using namespace std;

typedef unsigned char byte;

#ifndef MAIN_H
#define MAIN_H

struct saved_state 
{
    float angle;
    int32_t x;
    int32_t y;
};

// http://developer.android.com/reference/android/app/NativeActivity.html
struct engine 
{
    struct android_app* app;

    ASensorManager* sensorManager;
    const ASensor* accelerometerSensor;
    ASensorEventQueue* sensorEventQueue;

    int animating;
    EGLDisplay display;
    EGLSurface surface;
    EGLContext context;
    int32_t width;
    int32_t height;
    struct saved_state state;
};

#endif

extern float g_width;
extern float g_height;
extern bool g_quit;

extern AAssetManager* g_amgr;

extern unsigned int g_img;