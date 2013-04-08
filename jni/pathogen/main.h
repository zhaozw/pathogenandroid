
#ifndef MAIN_H
#define MAIN_H

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

extern "C" {
#include <png.h>
}

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
//#include <sys/socket.h>
//#include <netinet/in.h>
#include <limits.h>
#include <vector>

//#include <zip.h>

struct CVector2i;

using namespace std;

//#define SERVER_ADDR         "pathogen-game.com"
//#define SERVER_ADDR         "24.87.135.215"    //Test server
#define SERVER_ADDR         "pathogen.myftp.org"   //Test server
#define PORT                19023

#define FRAME_RATE          30
#define FRAME_INTERVAL		(1.0f/(float)FRAME_RATE)

#define RETRY_DELAY         20
#define CLIENT_TIMEOUT      (60*1000)

#define Z_FOV				45.0f
#define Z_TURN_RATE			1.0f
#define RUN_DSTAMINA		(1.0f / 5.0f)	//5 seconds to use up 1 stamina
#define Z_DAMAGE			30.0f
#define MELEE_D				50.0f
#define GRASP_D				25.0f
#define Z_ATTACK_DELAY		1000
#define INTERACTION_D		60.0f
#define VISIBLE_LIGHT		(50.0f/255.0f)

#define VERSION     2.0f

#define MAX_PATH	260

#define _isnan	isnan
#define stricmp strcasecmp

typedef unsigned char byte;
typedef unsigned int UINT;

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

class CTouch
{
public:
	bool on;
	int x, y;

	CTouch()
	{
		on = false;
	}

	CTouch(int X, int Y)
	{
		on = true;
		x = X;
		y = Y;
	}
};

extern struct engine* g_engine;
extern float g_width;
extern float g_height;
extern float g_near;
extern float g_far;
extern float g_fov;
extern float g_scale;
extern bool g_quit;

extern double g_FrameInterval;

enum GAMEMODE{LOADING, LOGO, INTRO, MENU, CONNECTING, PLAY};
extern enum GAMEMODE g_mode;

extern float g_reddening;
extern bool g_arrest;
extern int g_score;

enum VIEWMODE{FIRSTPERSON, THIRDPERSON};
extern int g_viewmode;

//#endif

//extern zip* APKArchive;
//extern char g_path[256];
//extern char g_tempPath[256];
//extern JNIEnv* g_env;
extern AAssetManager* g_amgr;

extern vector<CTouch> g_touch;

//extern bool g_inited;
//extern unsigned int g_img;
//extern unsigned int g_VBO;

void DummyVBO(unsigned int* vbo, GLenum usage = GL_DYNAMIC_DRAW);
void checkGlError(const char* op);
unsigned int timeGetTime();
unsigned int GetTickCount();
void Update();
void Draw();
void LoadingScreen();

#endif
