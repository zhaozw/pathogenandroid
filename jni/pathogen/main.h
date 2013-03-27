
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

#include <GLES/gl.h>
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

#include <zip.h>

using namespace std;

//#define SERVER_ADDR         @"pathogen-game.com"
//#define SERVER_ADDR         @"24.87.135.215"    //Test server
#define SERVER_ADDR         @"pathogen.myftp.org"   //Test server
#define PORT                19023

#define FRAME_RATE          30
#define FRAME_INTERVAL		(1000.0f/(float)FRAME_RATE)

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

#define stricmp strcasecmp

typedef unsigned char byte;
typedef unsigned int UINT;

extern float g_width;
extern float g_height;
extern float g_near;
extern float g_far;
extern float g_fov;

extern double g_FrameInterval;

enum GAMEMODE{LOGO, INTRO, MENU, CONNECTING, PLAY};
extern enum GAMEMODE g_mode;

extern float g_reddening;
extern bool g_arrest;
extern int g_score;

enum VIEWMODE{FIRSTPERSON, THIRDPERSON};
extern int g_viewmode;

extern zip* APKArchive;
extern char g_path[256];
extern char g_tempPath[256];
extern JNIEnv* g_env;
extern AAssetManager* g_amgr;

extern int g_width;
extern int g_height;
extern bool g_inited;

unsigned int timeGetTime();
unsigned int GetTickCount();

#endif