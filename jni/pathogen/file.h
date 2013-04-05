

#ifndef FILE_H
#define FILE_H

#include <string>
#include <sstream>
#include <iostream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

using namespace std;

//#define MAX_PATH 128

class CFile
{
public:
	//unsigned char* data;
	//int fsize;
	//int position;
	AAsset* mFile;

	CFile();
	CFile(const char* filepath, int mode=AASSET_MODE_UNKNOWN);
	~CFile();
	void open(const char* filepath, int mode=AASSET_MODE_UNKNOWN);
	void write(const char* filepath, char* nativepath);
	int seek(int off, int origin=SEEK_SET);
	//void seekend();
	int read(void* to, int amt);
	int tell();
	int remain();
	void close();
};

void StripPathExtension(const char* n, char* o);
void StripPath(const char* n, char* o);

#endif