
#include "main.h"
//#include "font.h"
#include "image.h"
#include "file.h"
#include "logger.h"

CFile::CFile()
{
	mFile = NULL;
}

void CFile::close()
{
	if(mFile)
	{
		AAsset_close(mFile);
		mFile = 0;
	}
}

CFile::CFile(const char* filepath, int mode)
{
	mFile = AAssetManager_open(g_amgr, filepath, mode);
}

CFile::~CFile()
{
	close();
}

int CFile::seek(int off, int origin)
{
	if(mFile)
	{
		return AAsset_seek(mFile, off, origin);
	}
	return 0;
}

int CFile::read(void* to, int amt)
{
	if(mFile)
	{
		return AAsset_read(mFile, to, amt);
	}
	return 0;
}

int CFile::tell()
{
	if(mFile)
	{
		off_t asset_l = AAsset_getLength(mFile);
		off_t asset_r = AAsset_getRemainingLength(mFile);
		return asset_l - asset_r;
	}
	return 0;
}

int CFile::remain()
{
	if(mFile)
	{
		return AAsset_getRemainingLength(mFile);
	}
	return 0;
}