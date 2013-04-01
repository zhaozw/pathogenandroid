
#include <GLES/gl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <string.h>
#include <iostream>
#include <fstream>

using namespace std;

#define SHMODE_ORTHO	0
#define SHMODE_MAP		1
#define SHMODE_MODEL	2

enum SLOT{POSITION, COLOR, TEXTURE, TEXTURE2, TEXCOORD, TEXCOORD2, PROJECTION, MODELMAT, VIEWMAT, WIDTH, HEIGHT, CAMERAPOS, SCALE, SHADERMODE, SLOTS};
enum SHADER{ORTHO, SKY, MAP, MODEL, BILLBOARD, OMNI, SHADERS};

extern unsigned int g_slots[SHADERS][SLOTS];
extern unsigned int g_program[SHADERS];

#define USE_OMNI

void LoadShaders();