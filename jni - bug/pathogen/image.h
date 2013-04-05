#ifndef IMAGE_H
#define IMAGE_H

#include <stdio.h>
#include <jpeglib.h>
#include "file.h"
#include "main.h"

struct tImage
{
	int channels;			// The channels in the image (3 = RGB : 4 = RGBA)
	int sizeX;				// The width of the image in pixels
	int sizeY;				// The height of the image in pixels
	unsigned char *data;	// The image pixel data
};

unsigned int CreateTexture(const char* strFileName);

#endif