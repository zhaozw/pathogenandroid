
#include "main.h"
#include "font.h"
#include "image.h"
#include "file.h"

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
	data = NULL;
	fsize = 0;
	position = 0;
}

CFile::CFile(const char* filepath)
{
	data = NULL;
	fsize = 0;
	position = 0;

	//zip_file* file = zip_fopen(APKArchive, filepath, 0);
	AAsset* asset_file = AAssetManager_open(g_amgr, filepath, AASSET_MODE_UNKNOWN); 

	//if(!file)
	if(!asset_file)
		return;

	char buff[256];
	char* oldbuf = NULL;
	char* newbuf = NULL;

	int r=0;
	//while((r=zip_fread(file, buff, 256)) > 0)
	while((r = AAsset_read(asset_file, buff, 256)) > 0)
	{
		newbuf = new char[fsize+r];

		if(oldbuf)
		{
			memcpy(newbuf, oldbuf, fsize);
		}

		memcpy(&newbuf[fsize], buff, r);

		delete [] oldbuf;
		oldbuf = newbuf;

		fsize += r;
	}

	data = newbuf;

	//zip_fclose(file);
	AAsset_close(asset_file);
}

void CFile::write(const char* filepath, char* nativepath)
{
	char raw[32];
	StripPath(filepath, raw);
	sprintf(nativepath, "%s/%s", g_tempPath, raw);
	FILE* fp = fopen(nativepath, "wb");
	fwrite(data, fsize, 1, fp);
	fclose(fp);
}

CFile::~CFile()
{
	if(data)
	{
		delete [] data;
		data = NULL;
	}
}

void CFile::seek(int off)
{
	position = off;
}

void CFile::seekend()
{
	position = fsize;
}

int CFile::read(void* to, int amt)
{
	int read = amt;

	if(read > fsize-position)
		read = fsize-position;

	memcpy(to, &data[position], read);
	position += read;

	return read;
}

int CFile::tell()
{
	return position;
}