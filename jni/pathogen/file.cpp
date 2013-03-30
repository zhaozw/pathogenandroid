
#include "main.h"
#include "font.h"
#include "image.h"
#include "file.h"
#include "logger.h"

void StripPathExtension(const char* n, char* o)
{
	string s0(n);
	size_t sep = s0.find_last_of("\\/");
	string s1;

    if (sep != std::string::npos)
        s1 = s0.substr(sep + 1, s0.size() - sep - 1);
	else
		s1 = s0;

	size_t dot = s1.find_last_of(".");
	string s2;

	if (dot != std::string::npos)
		s2 = s1.substr(0, dot);
	else
		s2 = s1;

	strcpy(o, s2.c_str());
}

void StripPath(const char* n, char* o)
{
	string s0(n);
	size_t sep = s0.find_last_of("\\/");
	string s1;

    if (sep != std::string::npos)
        s1 = s0.substr(sep + 1, s0.size() - sep - 1);
	else
		s1 = s0;

	strcpy(o, s1.c_str());
}

CFile::CFile()
{
	//data = NULL;
	//fsize = 0;
	//position = 0;
	mFile = NULL;
}

void CFile::close()
{
	/*
	if(data)
		delete [] data;

	fsize = 0;
	position = 0;
	*/
	if(mFile)
	{
		AAsset_close(mFile);
		mFile = 0;
	}
}

// http://minigamestudio.googlecode.com/svn-history/r407/trunk/source/render/android/ola_afile.cpp

CFile::CFile(const char* filepath, int mode)
{
	/*
	data = NULL;
	fsize = 0;
	position = 0;

	//zip_file* file = zip_fopen(APKArchive, filepath, 0);
	AAsset* asset_file = AAssetManager_open(g_amgr, filepath, AASSET_MODE_UNKNOWN); 

	//if(!file)
	if(!asset_file)
		return;
*/
	/*
	unsigned char buff[256];
	unsigned char* oldbuf = NULL;
	unsigned char* newbuf = NULL;

	int r=0;
	//while((r=zip_fread(file, buff, 256)) > 0)
	while((r = AAsset_read(asset_file, buff, 256)) > 0)
	{
		newbuf = new unsigned char[fsize+r];

		//buff[r] = '\0';
		//LOGI("%s", buff);

		LOGI("read %d", r);

		//if(oldbuf)
		if(fsize > 0)
		{
			memcpy(newbuf, oldbuf, fsize);
		}

		memcpy(&newbuf[fsize], buff, r);

		//if(oldbuf)
		if(fsize > 0)
		{
			delete [] oldbuf;
		}
		oldbuf = newbuf;

		fsize += r;
	}

	data = newbuf;
	*/
/*
	fsize = AAsset_getLength(asset_file);
	data = new unsigned char[fsize+1];
	AAsset_read(asset_file, data, fsize);
	data[fsize] = '\0';

	for(int i=0; i<fsize; i++)
	{
		if((char)data[i] == '\0')
			data[i] = (unsigned char)'!';
	}
	
	LOGI("%s size=%d", filepath, fsize);

	//char show[fsize];
	//memcpy(show, data, fsize);
	LOGI("%s", (char*)data);

	//zip_fclose(file);
	AAsset_close(asset_file);
	*/

	
	//LOGI("file() %s 0", filepath);

	//mFile = AAssetManager_open(g_amgr, filepath, AASSET_MODE_UNKNOWN);
	mFile = AAssetManager_open(g_amgr, filepath, mode);

	//LOGI("file() %s 1", filepath);
}

void CFile::write(const char* filepath, char* nativepath)
{
	/*
	char raw[32];
	StripPath(filepath, raw);
	sprintf(nativepath, "%s/%s", g_tempPath, raw);
	FILE* fp = fopen(nativepath, "wb");
	fwrite(data, fsize, 1, fp);
	fclose(fp);*/
}

CFile::~CFile()
{
	/*
	if(data)
	{
		delete [] data;
		data = NULL;
	}*/
	//close();
}

int CFile::seek(int off, int origin)
{
	//position = off;
	
	if(mFile)
	{
		//int origin = SEEK_SET;
		//SEEK_SET
		return AAsset_seek(mFile, off, origin);
		//return AAsset_seek(mFile, off, SEEK_SET);
	}
	return 0;
}

//void CFile::seekend()
//{
	//position = fsize;
//}

int CFile::read(void* to, int amt)
{
	/*
	int read = amt;

	if(read > fsize-position)
		read = fsize-position;

	memcpy(to, &data[position], read);
	position += read;

	return read;*/
	if(mFile)
	{
		return AAsset_read(mFile, to, amt);
	}
	return 0;
}

int CFile::tell()
{
	//return position;
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