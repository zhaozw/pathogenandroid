

#ifndef FILE_H
#define FILE_H

#include <string>
#include <sstream>
#include <iostream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

using namespace std;

class CFile
{
public:
	AAsset* mFile;

	CFile();
	CFile(const char* filepath, int mode=AASSET_MODE_UNKNOWN);
	~CFile();
	int seek(int off, int origin=SEEK_SET);
	int read(void* to, int amt);
	int tell();
	int remain();
	void close();
};

#endif