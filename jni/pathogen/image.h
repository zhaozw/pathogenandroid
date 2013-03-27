#ifndef IMAGE_H
#define IMAGE_H

#include <stdio.h>
#include <jpeglib.h>
#include "file.h"
#include "main.h"

class CTexture
{
public:
    bool on;
    char filepath[MAX_PATH];
    unsigned int tex;
    
    CTexture()
    {
        on = false;
    }
};

#define TEXTURES    2048

#define TGA_RGB		 2		// This tells us it's a normal RGB (really BGR) file
#define TGA_A		 3		// This tells us it's a ALPHA file
#define TGA_RLE		10		// This tells us that the targa is Run-Length Encoded (RLE)

#define JPEG_BUFFER_SIZE (8 << 10)

#define png_infopp_NULL (png_infopp)NULL
#define int_p_NULL (int*)NULL
#define png_voidp_NULL	(png_voidp)NULL

typedef struct 
{
    struct jpeg_source_mgr  pub;
} JPEGSource;

struct tImage
{
	int channels;			// The channels in the image (3 = RGB : 4 = RGBA)
	int sizeX;				// The width of the image in pixels
	int sizeY;				// The height of the image in pixels
	unsigned char *data;	// The image pixel data
};

struct tTextureToLoad
{
	unsigned int* tex;
	char filepath[MAX_PATH];
	bool clamp;
};

extern CTexture g_texture[TEXTURES];
extern int g_texwidth;
extern int g_texheight;
extern bool g_lastTexTransp;

//tImage *LoadBMP(const char *strFileName);
//tImage *LoadTGA(const char *strFileName);
//void DecodeJPG(jpeg_decompress_struct* cinfo, tImage *pImageData);
tImage *LoadJPG(const char *strFileName);
tImage *LoadPNG(const char *strFileName);

void FreeTexture(const char* filepath);
void FreeTextures();
void FindTextureExtension(char* strFileName);
unsigned int CreateTexture(const char* strFileName, bool search=true);

#endif