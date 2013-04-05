
#include <GLES/gl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <string.h>
#include <iostream>
#include <fstream>

// Uniforms and vertex attribs
enum SLOT{POSITION, TEXTURE, TEXTURE2, TEXCOORD, PROJECTION, MODELMAT, VIEWMAT, WIDTH, HEIGHT, SLOTS};

// Shaders
enum SHADER{ORTHO, MODEL,  SHADERS};

extern unsigned int g_slots[SHADERS][SLOTS];
extern unsigned int g_program[SHADERS];

void LoadShaders();