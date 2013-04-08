#include "image.h"
//#include "quake3BSP.h"
#include "main.h"
//#include "entity.h"
#include "logger.h"

CTexture g_texture[TEXTURES];

int g_texwidth;
int g_texheight;
int g_foundTex = 0;
bool g_lastTexTransp = false;
//zip_file* g_file;
char jpegBuffer[JPEG_BUFFER_SIZE];
JPEGSource   jpegSource;
//static FILE* g_src;
CFile g_src;
int srcLen;

/*
tImage *LoadBMP(const char *strFileName)
{
	AUX_RGBImageRec *pBitmap = NULL;
	FILE *pFile = NULL;

	// Open a file pointer to the BMP file and check if it was found and opened 
	if((pFile = fopen(strFileName, "rb")) == NULL) 
	{
		// Display an error message saying the file was not found, then return NULL
		MessageBox(g_hWnd, ("Unable to load BMP File!"), ("Error"), MB_OK);
		return NULL;
	}

	// Load the bitmap using the aux function stored in glaux.lib
	pBitmap = auxDIBImageLoad(strFileName);				

	// Allocate the tImage
	tImage *pImage = (tImage *)malloc(sizeof(tImage));

	// Assign the channels, width, height and image bits to pImage
	pImage->channels = 3;
	pImage->sizeX = pBitmap->sizeX;
	pImage->sizeY = pBitmap->sizeY;
	pImage->data  = pBitmap->data;

	// Free the bitmap pointer (The "data" will be freed later in CreateTexture())
	free(pBitmap);
	
	/*
	int stride = pImage->channels * pBitmap->sizeX;
	int i;
	int y2;
	int temp;

	for(int y = 0; y < pImage->sizeY/2; y++)
	{
		y2 = pImage->sizeY - y;
		// Store a pointer to the current line of pixels
		unsigned char *pLine = &(pImage->data[stride * y]);
		unsigned char *pLine2 = &(pImage->data[stride * y2]);
			
		// Go through all of the pixels and swap the B and R values since TGA
		// files are stored as BGR instead of RGB (or use GL_BGR_EXT verses GL_RGB)
		for(i = 0; i < stride; i += pImage->channels)
		{
			temp = pLine[i];
			pLine[i] = pLine2[i];
			pLine2[i] = temp;
			
			temp = pLine[i+1];
			pLine[i+1] = pLine2[i+1];
			pLine2[i+1] = temp;
			
			temp = pLine[i+2];
			pLine[i+2] = pLine2[i+2];
			pLine2[i+2] = temp;
		}
	}*/
/*
	return pImage;
}
*/
/*
tImage *LoadTGA(const char *strFileName)
{
	tImage *pImageData = NULL;			// This stores our important image data
	WORD width = 0, height = 0;			// The dimensions of the image
	byte length = 0;					// The length in bytes to the pixels
	byte imageType = 0;					// The image type (RLE, RGB, Alpha...)
	byte bits = 0;						// The bits per pixel for the image (16, 24, 32)
	FILE *pFile = NULL;					// The file pointer
	int channels = 0;					// The channels of the image (3 = RGA : 4 = RGBA)
	int stride = 0;						// The stride (channels * width)
	int i = 0;							// A counter

	// Open a file pointer to the targa file and check if it was found and opened 
	if((pFile = fopen(strFileName, "rb")) == NULL) 
	{
		// Display an error message saying the file was not found, then return NULL
		MessageBox(g_hWnd, ("Unable to load TGA File!"), ("Error"), MB_OK);
		return NULL;
	}
		
	// Allocate the structure that will hold our eventual image data (must free it!)
	pImageData = (tImage*)malloc(sizeof(tImage));

	// Read in the length in bytes from the header to the pixel data
	fread(&length, sizeof(byte), 1, pFile);
	
	// Jump over one byte
	fseek(pFile,1,SEEK_CUR); 

	// Read in the imageType (RLE, RGB, etc...)
	fread(&imageType, sizeof(byte), 1, pFile);
	
	// Skip past general information we don't care about
	fseek(pFile, 9, SEEK_CUR); 

	// Read the width, height and bits per pixel (16, 24 or 32)
	fread(&width,  sizeof(WORD), 1, pFile);
	fread(&height, sizeof(WORD), 1, pFile);
	fread(&bits,   sizeof(byte), 1, pFile);
	
	// Now we move the file pointer to the pixel data
	fseek(pFile, length + 1, SEEK_CUR); 

	// Check if the image is RLE compressed or not
	if(imageType != TGA_RLE)
	{
		// Check if the image is a 24 or 32-bit image
		if(bits == 24 || bits == 32)
		{
			// Calculate the channels (3 or 4) - (use bits >> 3 for more speed).
			// Next, we calculate the stride and allocate enough memory for the pixels.
			channels = bits / 8;
			stride = channels * width;
			pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*stride*height));

			// Load in all the pixel data line by line
			for(int y = 0; y < height; y++)
			{
				// Store a pointer to the current line of pixels
				unsigned char *pLine = &(pImageData->data[stride * y]);

				// Read in the current line of pixels
				fread(pLine, stride, 1, pFile);
			
				// Go through all of the pixels and swap the B and R values since TGA
				// files are stored as BGR instead of RGB (or use GL_BGR_EXT verses GL_RGB)
				for(i = 0; i < stride; i += channels)
				{
					int temp     = pLine[i];
					pLine[i]     = pLine[i + 2];
					pLine[i + 2] = temp;
				}
			}
		}
		// Check if the image is a 16 bit image (RGB stored in 1 unsigned short)
		else if(bits == 16)
		{
			unsigned short pixels = 0;
			int r=0, g=0, b=0;

			// Since we convert 16-bit images to 24 bit, we hardcode the channels to 3.
			// We then calculate the stride and allocate memory for the pixels.
			channels = 3;
			stride = channels * width;
			pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*stride*height));

			// Load in all the pixel data pixel by pixel
			for(int i = 0; i < width*height; i++)
			{
				// Read in the current pixel
				fread(&pixels, sizeof(unsigned short), 1, pFile);
				
				// Convert the 16-bit pixel into an RGB
				b = (pixels & 0x1f) << 3;
				g = ((pixels >> 5) & 0x1f) << 3;
				r = ((pixels >> 10) & 0x1f) << 3;
				
				// This essentially assigns the color to our array and swaps the
				// B and R values at the same time.
				pImageData->data[i * 3 + 0] = r;
				pImageData->data[i * 3 + 1] = g;
				pImageData->data[i * 3 + 2] = b;
			}
		}	
		// Else return a NULL for a bad or unsupported pixel format
		else
			return NULL;
	}
	// Else, it must be Run-Length Encoded (RLE)
	else
	{
		// Create some variables to hold the rleID, current colors read, channels, & stride.
		byte rleID = 0;
		int colorsRead = 0;
		channels = bits / 8;
		stride = channels * width;

		// Next we want to allocate the memory for the pixels and create an array,
		// depending on the channel count, to read in for each pixel.
		pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*stride*height));
		byte *pColors = ((byte*)malloc(sizeof(byte)*channels));

		// Load in all the pixel data
		while(i < width*height)
		{
			// Read in the current color count + 1
			fread(&rleID, sizeof(byte), 1, pFile);
			
			// Check if we don't have an encoded string of colors
			if(rleID < 128)
			{
				// Increase the count by 1
				rleID++;

				// Go through and read all the unique colors found
				while(rleID)
				{
					// Read in the current color
					fread(pColors, sizeof(byte) * channels, 1, pFile);

					// Store the current pixel in our image array
					pImageData->data[colorsRead + 0] = pColors[2];
					pImageData->data[colorsRead + 1] = pColors[1];
					pImageData->data[colorsRead + 2] = pColors[0];

					// If we have a 4 channel 32-bit image, assign one more for the alpha
					if(bits == 32)
						pImageData->data[colorsRead + 3] = pColors[3];

					// Increase the current pixels read, decrease the amount
					// of pixels left, and increase the starting index for the next pixel.
					i++;
					rleID--;
					colorsRead += channels;
				}
			}
			// Else, let's read in a string of the same character
			else
			{
				// Minus the 128 ID + 1 (127) to get the color count that needs to be read
				rleID -= 127;

				// Read in the current color, which is the same for a while
				fread(pColors, sizeof(byte) * channels, 1, pFile);

				// Go and read as many pixels as are the same
				while(rleID)
				{
					// Assign the current pixel to the current index in our pixel array
					pImageData->data[colorsRead + 0] = pColors[2];
					pImageData->data[colorsRead + 1] = pColors[1];
					pImageData->data[colorsRead + 2] = pColors[0];

					// If we have a 4 channel 32-bit image, assign one more for the alpha
					if(bits == 32)
						pImageData->data[colorsRead + 3] = pColors[3];

					// Increase the current pixels read, decrease the amount
					// of pixels left, and increase the starting index for the next pixel.
					i++;
					rleID--;
					colorsRead += channels;
				}
				
			}
				
		}
	}

	// Close the file pointer that opened the file
	fclose(pFile);

	// Flip upside-down
	int x;
	int y2;
	byte temp[4];
	for(int y=0; y<height/2; y++)
	{
		y2 = height - y - 1;
		
		unsigned char *pLine = &(pImageData->data[stride * y]);
		unsigned char *pLine2 = &(pImageData->data[stride * y2]);

		for(x=0; x<width*channels; x+=channels)
		{
			temp[0] = pLine[x + 0];
			temp[1] = pLine[x + 1];
			temp[2] = pLine[x + 2];
			if(bits == 32)
				temp[3] = pLine[x + 3];
			
			pLine[x + 0] = pLine2[x + 0];
			pLine[x + 1] = pLine2[x + 1];
			pLine[x + 2] = pLine2[x + 2];
			if(bits == 32)
				pLine[x + 3] = pLine2[x + 3];
			
			pLine2[x + 0] = temp[0];
			pLine2[x + 1] = temp[1];
			pLine2[x + 2] = temp[2];
			if(bits == 32)
				pLine2[x + 3] = temp[3];
		}
	}

	// Fill in our tImage structure to pass back
	pImageData->channels = channels;
	pImageData->sizeX    = width;
	pImageData->sizeY    = height;

	// Return the TGA data (remember, you must free this data after you are done)
	return pImageData;
}
*/
bool source_init(const char *filename) 
{
	LOGI("source_init %s", filename);
	/*
    g_src = fopen(filename, "rb");

    if (g_src == NULL) return 0;

    fseek(g_src, 0, SEEK_END);

    srcLen = ftell(src);

    fseek(g_src, 0, SEEK_SET);
*/
	//g_src = CFile(filename, AASSET_MODE_RANDOM);
	//g_src = CFile(filename, AASSET_MODE_STREAMING);
	g_src = CFile(filename, AASSET_MODE_BUFFER);

	//if(g_src.fsize <= 0)
	if(!g_src.mFile)
		return false;

	srcLen = g_src.remain();

	LOGI("source_init size=%d", srcLen);

    return true;
}

void source_close() 
{
	LOGI("source_close");
    //fclose(g_src);
	g_src.close();
}

int source_read(char* buffer) 
{
    //int len = min(JPEG_BUFFER_SIZE, g_src.fsize-g_src.position);
	//int len = min(JPEG_BUFFER_SIZE, 

    //fread(buffer, len, 1, src);
	//g_src.read((void*)buffer, len);

    //return len;
	//LOGI("source_read");
	
	//return g_src.read((void*)buffer, JPEG_BUFFER_SIZE);

	LOGI("source_read...");
	
	int toread = JPEG_BUFFER_SIZE;
	if(g_src.remain() < toread)
		toread = g_src.remain();

	LOGI("source_read %d", toread);

	//return g_src.read((void*)buffer, toread);
	int ret = g_src.read((void*)buffer, toread);

	LOGI("read %d", ret);
	
	return ret;
}

void source_seek(int num) 
{
	LOGI("source_seek %d", num);
    //fseek(src, num, SEEK_CUR);
	g_src.seek(num, SEEK_CUR);
}

static void init_sourceFunc(j_decompress_ptr cinfo) 
{
	LOGI("init_sourceFunc");
    ((JPEGSource*)cinfo->src)->pub.bytes_in_buffer = 0;
}

static boolean fill_input_bufferFunc(j_decompress_ptr cinfo) 
{
	LOGI("fill_input_bufferFunc");
    JPEGSource  *src = (JPEGSource*)cinfo->src;

    src->pub.bytes_in_buffer = source_read(jpegBuffer);

    src->pub.next_input_byte = (const unsigned char*)jpegBuffer;

    return TRUE;
}

void skip_input_dataFunc(j_decompress_ptr cinfo, long num_bytes) 
{
	LOGI("skip_input_dataFunc %d", (int)num_bytes);
    JPEGSource  *src = (JPEGSource*)cinfo->src;

    if (num_bytes > 0) 
	{
        source_seek(num_bytes);

        if (num_bytes > src->pub.bytes_in_buffer) src->pub.bytes_in_buffer = 0;

        else 
		{
            src->pub.next_input_byte += num_bytes;
            src->pub.bytes_in_buffer -= num_bytes;
        }
    }
}

void term_sourceFunc(j_decompress_ptr cinfo) 
{
	LOGI("term_sourceFunc");
}

tImage *LoadJPG(const char *strFileName)
{
	//LOGI("JPG %s 0", strFileName);

	tImage *pImageData = NULL;
    struct jpeg_decompress_struct cinfo;

    jpeg_error_mgr jerr;
	
	//LOGI("JPEG_LIB_VERSION = %d", JPEG_LIB_VERSION);

	//LOGI("JPG %s 1", strFileName);

	/*
    if (!source_init(strFileName))
	{
		LOGE("Error opening jpeg %s", strFileName);
		return NULL;
	}

	
	LOGI("JPG %s 2", strFileName);
*/
	
	pImageData = (tImage*)malloc(sizeof(tImage));

    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_decompress(&cinfo); /*
    jpegSource.pub.init_source = init_sourceFunc;
    jpegSource.pub.fill_input_buffer = fill_input_bufferFunc;
    jpegSource.pub.skip_input_data = skip_input_dataFunc;
    jpegSource.pub.resync_to_restart = jpeg_resync_to_restart;
    jpegSource.pub.term_source = term_sourceFunc;
    jpegSource.pub.next_input_byte = NULL;
    jpegSource.pub.bytes_in_buffer = 0;
    cinfo.src = (struct jpeg_source_mgr*)&jpegSource;
	*/
	//LOGI("JPG %s 3", strFileName);

	
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

	//LOGI("JPG %s 3.1", strFileName);

    jpeg_start_decompress(&cinfo);
	
	//LOGI("JPG %s 4", strFileName);

    pImageData->channels = cinfo.num_components;
    pImageData->sizeX    = cinfo.image_width;
    pImageData->sizeY    = cinfo.image_height;

    //printf("%d %d\n", pImageData.sizeX, pImageData.sizeY);

    int rowSpan = cinfo.image_width * cinfo.num_components;
	
	//LOGI("JPG %s 5", strFileName);

    pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*rowSpan*pImageData->sizeY));

    unsigned char** rowPtr = new unsigned char*[pImageData->sizeY];

    for (int i = 0; i < pImageData->sizeY; i++)
        rowPtr[i] = &(pImageData->data[i * rowSpan]);
	
	//LOGI("JPG %s 6", strFileName);

    int rowsRead = 0;

    while (cinfo.output_scanline < cinfo.output_height)
        rowsRead += jpeg_read_scanlines(&cinfo, &rowPtr[rowsRead], cinfo.output_height - rowsRead);

	
	//LOGI("JPG %s 7", strFileName);

    delete [] rowPtr;

    //free(pImageData->data);

    jpeg_finish_decompress(&cinfo);
    jpeg_destroy_decompress(&cinfo);

	//LOGI("JPG %s 8", strFileName);

    //source_close();
       AAsset_close(pAsset);
	
	//LOGI("JPG %s 9", strFileName);

    return pImageData;
}

/*
void DecodeJPG(jpeg_decompress_struct* cinfo, tImage *pImageData)
{
	// Read in the header of the jpeg file
	jpeg_read_header(cinfo, TRUE);
	
	// Start to decompress the jpeg file with our compression info
	jpeg_start_decompress(cinfo);

	// Get the image dimensions and channels to read in the pixel data
	pImageData->channels = cinfo->num_components;
	pImageData->sizeX    = cinfo->image_width;
	pImageData->sizeY    = cinfo->image_height;

	// Get the row span in bytes for each row
	int rowSpan = cinfo->image_width * cinfo->num_components;
	
	// Allocate memory for the pixel buffer
	pImageData->data = ((unsigned char*)malloc(sizeof(unsigned char)*rowSpan*pImageData->sizeY));
			
	// Create an array of row pointers
	unsigned char** rowPtr = new unsigned char*[pImageData->sizeY];

	for (int i = 0; i < pImageData->sizeY; i++)
		rowPtr[i] = &(pImageData->data[i * rowSpan]);

	// Now comes the juice of our work, here we extract all the pixel data
	int rowsRead = 0;
	while (cinfo->output_scanline < cinfo->output_height) 
	{
		// Read in the current row of pixels and increase the rowsRead count
		rowsRead += jpeg_read_scanlines(cinfo, 
										&rowPtr[rowsRead], cinfo->output_height - rowsRead);
	}
	
	// Delete the temporary row pointers
	delete [] rowPtr;

	// Finish decompressing the data
	jpeg_finish_decompress(cinfo);
}
*/

void png_zip_read(png_structp png_ptr, png_bytep data, png_size_t length) 
{
  //zip_fread(g_file, data, length);
	g_src.read((void*)data, length);
}

//GLuint loadTextureFromPNG(const char* filename, int &width, int &height)
tImage *LoadPNG(const char *strFileName)
{
	//LOGI("PNG %s 0", strFileName);

	tImage *pImageData = NULL;

	//return NULL;

  //g_file = zip_fopen(APKArchive, strFileName, 0);

	g_src.close();

	//g_src = CFile(strFileName);
	g_src.open(strFileName);
	//CFile file(strFileName);
  //if (!g_file)
	//if(file.fsize <= 0)
	//if(g_src.fsize <= 0)
	if(!g_src.mFile)
  {
    //LOGE("Error opening %s from APK", strFileName);
    return NULL;
  }

  //LOGI("PNG_LIBPNG_VER_STRING = %s", PNG_LIBPNG_VER_STRING);

  //header for testing if it is a png
  png_byte header[8];

  //read the header
  //zip_fread(g_file, header, 8);
  //file.read(header, 8);
  //g_src.seek(0);
  g_src.read((void*)header, 8);


	//LOGI("PNG %s 1", strFileName);

  //test if png
  
  int is_png = !png_sig_cmp(header, 0, 8);
  if (!is_png) 
  {
	  //137 80 78 71 13 10 26 10
	  //ID=89h,'PNG',13,10,26,10
    //zip_fclose(g_file);
    LOGE("Not a png file : %s %d,%d,%d,%d,%d,%d,%d,%d", strFileName, 
		(int)header[0], (int)header[1], (int)header[2], (int)header[3], 
		(int)header[4], (int)header[5], (int)header[6], (int)header[7]);
		g_src.close();
    return NULL;
  }

  
	//LOGI("PNG %s 2", strFileName);

  //create png struct
  png_structp png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
  if (!png_ptr) 
  {
    //zip_fclose(g_file);
    LOGE("Unable to create png struct : %s", strFileName);
		g_src.close();
    return NULL;
  }

  
	//LOGI("PNG %s 3", strFileName);

  //create png info struct
  png_infop info_ptr = png_create_info_struct(png_ptr);
  if (!info_ptr) 
  {
    png_destroy_read_struct(&png_ptr, (png_infopp) NULL, (png_infopp) NULL);
    LOGE("Unable to create png info : %s", strFileName);
    //zip_fclose(g_file);
		g_src.close();
    return NULL;
  }


	//LOGI("PNG %s 4", strFileName);

  //create png info struct
  png_infop end_info = png_create_info_struct(png_ptr);
  if (!end_info) 
  {
    png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp) NULL);
    LOGE("Unable to create png end info : %s", strFileName);
    //zip_fclose(g_file);
		g_src.close();
    return NULL;
  }

  
	//LOGI("PNG %s 5", strFileName);

  //png error stuff, not sure libpng man suggests this.
  if (setjmp(png_jmpbuf(png_ptr))) 
  {
    //zip_fclose(g_file);
    LOGE("Error during setjmp : %s", strFileName);
    png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		g_src.close();
    return NULL;
  }

  
	//LOGI("PNG %s 6", strFileName);

  //init png reading
  //png_init_io(png_ptr, fp);
  png_set_read_fn(png_ptr, NULL, png_zip_read);

  
	//LOGI("PNG %s 7", strFileName);

    //unsigned int sig_read = 0;

  //let libpng know you already read the first 8 bytes
  png_set_sig_bytes(png_ptr, 8);
  //png_set_sig_bytes(png_ptr, sig_read);

  // read all the info up to the image data
  png_read_info(png_ptr, info_ptr);

  //variables to pass to get info
  int bit_depth, color_type;
  png_uint_32 twidth, theight;

  
	//LOGI("PNG %s 8", strFileName);

  // get info about png
  png_get_IHDR(png_ptr, info_ptr, &twidth, &theight, &bit_depth, &color_type, NULL, NULL, NULL);

	pImageData = (tImage*)malloc(sizeof(tImage));

	pImageData->sizeX = twidth;
	pImageData->sizeY = theight;

  switch( color_type )
	{
        case PNG_COLOR_TYPE_RGBA:
            pImageData->channels = 4;
            break;
        case PNG_COLOR_TYPE_RGB:
            pImageData->channels = 3;
            break;
        default:
			//g_log<<strFileName<<" color type "<<png_get_color_type(png_ptr, info_ptr)<<" not supported"<<endl;
            LOGE("%s color type %d not supported", strFileName, (int)png_get_color_type(png_ptr, info_ptr));
			//std::cout << "Color type " << info_ptr->color_type << " not supported" << std::endl;
            png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
            //fclose(fp);
			//zip_fclose(g_file);
			free(pImageData);
		g_src.close();
            return NULL;
    }

  //update width and height based on png info
  //width = twidth;
  //height = theight;

  
	//LOGI("PNG %s 9", strFileName);

  // Update the png info struct.
  png_read_update_info(png_ptr, info_ptr);

  // Row size in bytes.
  int row_bytes = png_get_rowbytes(png_ptr, info_ptr);

  
	pImageData->data = (unsigned char*) malloc(row_bytes * pImageData->sizeY);

  // Allocate the image_data as a big block, to be given to opengl
  //png_byte *image_data = new png_byte[rowbytes * height];
  //if (!image_data) 
	if(!pImageData->data)
  {
    //clean up memory and close stuff
    png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
    LOGE("Unable to allocate image_data while loading %s ", strFileName);
    //zip_fclose(g_file);
	free(pImageData);
		g_src.close();
    return NULL;
  }

	
	//LOGI("PNG %s 10", strFileName);
///*
	//Variant 1
  //row_pointers is for pointing to image_data for reading the png with libpng
  png_bytep *row_pointers = new png_bytep[pImageData->sizeY];
  //png_bytep **row_pointers = new png_bytep*[pImageData->sizeY];
  if (!row_pointers) 
  {
    //clean up memory and close stuff
    png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
    //delete[] image_data;
	free(pImageData->data);
	free(pImageData);
    LOGE("Unable to allocate row_pointer while loading %s ", strFileName);
    //zip_fclose(g_file);
		g_src.close();
    return NULL;
  }
  // set the individual row_pointers to point at the correct offsets of image_data
  for (int i = 0; i < pImageData->sizeY; ++i)
    //row_pointers[height - 1 - i] = image_data + i * rowbytes;
	//row_pointers[pImageData->sizeY - 1 - i] = pImageData->data + i * row_bytes;
	row_pointers[i] = pImageData->data + i * row_bytes;
	//row_pointers[pImageData->sizeY - 1 - i] = &(pImageData->data[ i * row_bytes ]);


	//LOGI("PNG %s 11", strFileName);

  //read the png into image_data through row_pointers
  png_read_image(png_ptr, row_pointers);
//*/

/*
//Variant 2
	  // If you have enough memory to read
     // in the entire image at once, and
     // you need to specify only
     // transforms that can be controlled
     // with one of the PNG_TRANSFORM_*
     // bits (this presently excludes
     // dithering, filling, setting
     // background, and doing gamma
     // adjustment), then you can read the
     // entire image (including pixels)
     // into the info structure with this
     // call
     //
     // PNG_TRANSFORM_STRIP_16 |
     // PNG_TRANSFORM_PACKING  forces 8 bit
     // PNG_TRANSFORM_EXPAND forces to
     //  expand a palette into RGB
   	png_read_png(png_ptr, info_ptr, PNG_TRANSFORM_STRIP_16 | PNG_TRANSFORM_PACKING | PNG_TRANSFORM_EXPAND, png_voidp_NULL);

	png_bytepp row_pointers = png_get_rows(png_ptr, info_ptr);
	
	for (int i = 0; i < pImageData->sizeY; i++) 
	{
        // note that png is ordered top to
        // bottom, but OpenGL expect it bottom to top
        // so the order or swapped
		
        memcpy((void*)(pImageData->data+(row_bytes * i)), row_pointers[i], row_bytes);
        //memcpy((void*)(pImageData->data+(row_bytes * (pImageData->sizeY-1-i))), row_pointers[i], row_bytes);
    }
*/
  //Now generate the OpenGL texture object
  //GLuint texture;
  //glGenTextures(1, &texture);
  //glBindTexture(GL_TEXTURE_2D, texture);
  //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (GLvoid*) image_data);
  //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);


	//LOGI("PNG %s 12", strFileName);

  //clean up memory and close stuff
  png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
  //delete[] image_data;
  delete[] row_pointers;
  //zip_fclose(g_file);
		g_src.close();


	//LOGI("PNG %s 13", strFileName);

		//LOGI("%s w,h,c=%d,%d,%d", strFileName, (int)pImageData->sizeX, (int)pImageData->sizeY, (int)pImageData->channels);

  //return texture;
  return pImageData;
}

void FindTextureExtension(char *strFileName)
{
	char strJPGPath[MAX_PATH] = {0};
	char strPNGPath[MAX_PATH] = {0};
	//char strTGAPath[MAX_PATH] = {0}; 
	//char strBMPPath[MAX_PATH] = {0}; 
	//zip_file* fp = NULL;
	AAsset* fp = NULL;

	//GetCurrentDirectory(MAX_PATH, strJPGPath);

	//strcat(strJPGPath, "/");
	strcat(strJPGPath, strFileName);
	//strcpy(strTGAPath, strFileName);
	//strcpy(strBMPPath, strFileName);
	strcpy(strPNGPath, strFileName);
	
	strcat(strJPGPath, ".jpg");
	//strcat(strTGAPath, ".tga");
	//strcat(strBMPPath, ".bmp");
	strcat(strPNGPath, ".png");

	//if((fp = zip_fopen(APKArchive, strJPGPath, 0)) != NULL)
	if((fp = AAssetManager_open(g_amgr, strJPGPath, AASSET_MODE_UNKNOWN)) != NULL)
	{
		//zip_fclose(fp);
		AAsset_close(fp);
		strcat(strFileName, ".jpg");
		return;
	}
	
	//if((fp = zip_fopen(APKArchive, strPNGPath, 0)) != NULL)
	if((fp = AAssetManager_open(g_amgr, strPNGPath, AASSET_MODE_UNKNOWN)) != NULL)
	{
		//zip_fclose(fp);
		AAsset_close(fp);
		strcat(strFileName, ".png");
		return;
	}

	//if((fp = zip_fopen(APKArchive, strTGAPath, 0)) != NULL)
	{
	//	zip_fclose(fp);
	//	strcat(strFileName, ".tga");
	//	return;
	}

	//if((fp = zip_fopen(APKArchive, strBMPPath, 0)) != NULL)
	{
	//	zip_fclose(fp);
	//	strcat(strFileName, ".bmp");
	//	return;
	}
}

bool FindTexture(const char* fullpath, unsigned int* tex)
{
    for(int i=0; i<TEXTURES; i++)
    {
        if(!g_texture[i].on)
            continue;
        
        if(stricmp(fullpath, g_texture[i].filepath) == 0)
        {
            (*tex) = g_texture[i].tex;
            return true;
        }
    }
    
    return false;
}

int NewTexture()
{
    for(int i=0; i<TEXTURES; i++)
        if(!g_texture[i].on)
            return i;
    
	LOGE("No more textues!");
    return -1;
}

void FreeTexture(const char* filepath)
{
    char full[128];
	strcpy(full, filepath);
    FindTextureExtension(full);
    //NSString* fullName = [[NSString alloc] initWithFormat:@"%@.%@", nameStripped, type];
    
	for(int i=0; i<TEXTURES; i++)
    {
        if(!g_texture[i].on)
            continue;
        
        //NSLog(@"Test %d", i);
        //NSLog(@"Testing texture %s / %s", filepath, [g_texture[i].filepath UTF8String]);
		//if(g_texture[i].on && stricmp([g_texture[i].filepath UTF8String], filepath) == 0)
        if(stricmp(full, g_texture[i].filepath) == 0)
		{
            
            //NSLog(@"Freeing texture %s / %s", filepath, [g_texture[i].filepath UTF8String]);
            
			g_texture[i].on = false;
			glDeleteTextures(1, &g_texture[i].tex);
			//g_log<<"Found texture "<<filepath<<" ("<<texture<<")"<<endl;
			return;
		}
    }
    
    //NSLog(@"Couldn't free texture %s / %@", filepath, fullName);
}

int NewTexture(const char* fullpath, unsigned int tex)
{
    int i = NewTexture();
    if(i < 0)
        return -1;
    
    g_texture[i].on = true;
    strcpy(g_texture[i].filepath, fullpath);
    g_texture[i].tex = tex;
    
    return i;
}

void FreeTextures()
{
    for(int i=0; i<TEXTURES; i++)
    {
        if(!g_texture[i].on)
            continue;
        
        glDeleteTextures(1, &g_texture[i].tex);

		g_texture[i].on = false;
    }
}

unsigned int CreateTexture(const char* strFileName, bool search)
{
 	if(!strFileName) 
		return false;

	unsigned int texture;

	//LOGI("tex 1");

	char fullName[256];
	strcpy(fullName, strFileName);
	FindTextureExtension(fullName);

	//LOGI("tex 2");
	/*
	if(id < 0)
		if(FindTexture(fullName, &texture))
			return texture;
*/

	if(search && FindTexture(fullName, &texture))
        return texture;

	//LOGI("tex 3");
	// Define a pointer to a tImage
	tImage *pImage = NULL;

	
	//LOGI("tex 4");

	//bool ispng = false;
	// If the file is a jpeg, load the jpeg and store the data in pImage
	if(strstr(fullName, ".jpg"))
	{
		pImage = LoadJPG(fullName);
	}
	else if(strstr(fullName, ".png"))
	{
		pImage = LoadPNG(fullName);
		//ispng = true;
	}
	//LOGI("tex 5");
	// If the file is a tga, load the tga and store the data in pImage
	//else if(strstr(fullName, ".tga"))
	//{
	//	pImage = LoadTGA(fullName);
	//}
	// If the file is a bitmap, load the bitmap and store the data in pImage
	//else if(strstr(strFileName, ".bmp"))
	//{
	//	pImage = LoadBMP(strFileName);
	//}

	// Make sure valid image data was given to pImage, otherwise return false
	if(pImage == NULL)		
	{
		LOGE("Failed to load %s", fullName);
		return false;
	}

	// Generate a texture with the associative texture ID stored in the array
	glGenTextures(1, &texture);

	// This sets the alignment requirements for the start of each pixel row in memory.
	//glPixelStorei (GL_UNPACK_ALIGNMENT, 1);

	// Bind the texture to the texture arrays index and init the texture
	glBindTexture(GL_TEXTURE_2D, texture);

	// Assume that the texture is a 24 bit RGB texture (We convert 16-bit ones to 24-bit)
	int textureType = GL_RGB;

	// If the image is 32-bit (4 channels), then we need to specify GL_RGBA for an alpha
	if(pImage->channels == 4)
		textureType = GL_RGBA;
		
	// Option 1: with mipmaps
	//gluBuild2DMipmaps(GL_TEXTURE_2D, pImage->channels, pImage->sizeX, pImage->sizeY, textureType, GL_UNSIGNED_BYTE, pImage->data);
	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
	//glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

	//if(clamp)
	{
	//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
	//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
	}
	//else
	{
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	}
	
	// Option 2: without mipmaps
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, textureType, pImage->sizeX, pImage->sizeY, 0, textureType, GL_UNSIGNED_BYTE, pImage->data);
	//glTexImage2D(GL_TEXTURE_2D, 0, textureType, pImage->sizeX, pImage->sizeY, 0, GL_RGBA, GL_UNSIGNED_BYTE, pImage->data);

	// Now we need to free the image data that we loaded since openGL stored it as a texture
	if (pImage)										// If we loaded the image
	{
		g_texwidth = pImage->sizeX;
		g_texheight = pImage->sizeY;

		if (pImage->data)							// If there is texture data
		{
			//if(ispng)
			//	LOGI("free png data");
			free(pImage->data);						// Free the texture data, we don't need it anymore
		}

		free(pImage);								// Free the image structure

		LOGI("%s", fullName);
	}

    if(search)
        NewTexture(fullName, texture);

	//if(ispng)
	//	LOGI("png tex = %d", (int)texture);

	// Return a success
	return texture;
}
