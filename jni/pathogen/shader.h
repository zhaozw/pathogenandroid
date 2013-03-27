
#include <GLES/gl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <string.h>
#include <iostream>
#include <fstream>

using namespace std;

enum SLOT{POSITION, COLOR, TEXTURE, TEXTURE2, TEXCOORD, TEXCOORD2, PROJECTION, MODELMAT, VIEWMAT, WIDTH, HEIGHT, CAMERAPOS, SCALE, SLOTS};
enum SHADER{SKY, MAP, MODEL, BILLBOARD, ORTHO, SHADERS};

extern unsigned int g_slots[SHADERS][SLOTS];
extern unsigned int g_program[SHADERS];

void LoadShaders();