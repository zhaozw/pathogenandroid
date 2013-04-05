#include "image.h"
#include "main.h"
#include "logger.h"

tImage *LoadJPG(const char *strFileName)
{
	tImage *pImageData = NULL;
    struct jpeg_decompress_struct cinfo;

    jpeg_error_mgr jerr;
	
	pImageData = (tImage*)malloc(sizeof(tImage));

    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_decompress(&cinfo);
	
	AAsset* pAsset = AAssetManager_open(g_amgr, strFileName, AASSET_MODE_UNKNOWN);
    if (!pAsset)
	{
		free(pImageData);
		LOGE("Error opening jpeg %s", strFileName);
		return NULL;
	}

	unsigned char* ucharRawData = (unsigned char*)AAsset_getBuffer(pAsset);
    long myAssetLength = (long)AAsset_getLength(pAsset);
	
	// the jpeg_stdio_src alternative func, which is also included in IJG's lib.
    jpeg_mem_src(&cinfo, ucharRawData, myAssetLength);

    jpeg_read_header(&cinfo, TRUE);

    jpeg_start_decompress(&cinfo);

    pImageData->channels = cinfo.num_components;
    pImageData->sizeX    = cinfo.image_width;
    pImageData->sizeY    = cinfo.image_height;

    int rowSpan = cinfo.image_width * cinfo.num_components;

    pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*rowSpan*pImageData->sizeY));

    unsigned char** rowPtr = new unsigned char*[pImageData->sizeY];

    for (int i = 0; i < pImageData->sizeY; i++)
        rowPtr[i] = &(pImageData->data[i * rowSpan]);

    int rowsRead = 0;

    while (cinfo.output_scanline < cinfo.output_height)
        rowsRead += jpeg_read_scanlines(&cinfo, &rowPtr[rowsRead], cinfo.output_height - rowsRead);

    delete [] rowPtr;

    jpeg_finish_decompress(&cinfo);
    jpeg_destroy_decompress(&cinfo);
    
	AAsset_close(pAsset);

    return pImageData;
}

unsigned int CreateTexture(const char* strFileName)
{
 	if(!strFileName) 
		return false;

	unsigned int texture;

	tImage *pImage = NULL;

	if(strstr(strFileName, ".jpg"))
	{
		pImage = LoadJPG(strFileName);
	}

	// Make sure valid image data was given to pImage, otherwise return false
	if(pImage == NULL)		
	{
		LOGE("Failed to load %s", strFileName);
		return false;
	}

	// Generate a texture with the associative texture ID stored in the array
	glGenTextures(1, &texture);

	// This sets the alignment requirements for the start of each pixel row in memory.
	glPixelStorei (GL_UNPACK_ALIGNMENT, 1);

	// Bind the texture to the texture arrays index and init the texture
	glBindTexture(GL_TEXTURE_2D, texture);

	// Assume that the texture is a 24 bit RGB texture (We convert 16-bit ones to 24-bit)
	int textureType = GL_RGB;

	// If the image is 32-bit (4 channels), then we need to specify GL_RGBA for an alpha
	if(pImage->channels == 4)
		textureType = GL_RGBA;
		
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, textureType, pImage->sizeX, pImage->sizeY, 0, textureType, GL_UNSIGNED_BYTE, pImage->data);

	// Now we need to free the image data that we loaded since openGL stored it as a texture
	if (pImage)										// If we loaded the image
	{
		if (pImage->data)							// If there is texture data
		{
			free(pImage->data);						// Free the texture data, we don't need it anymore
		}

		free(pImage);								// Free the image structure

		LOGI("%s", strFileName);
	}

	return texture;
}
