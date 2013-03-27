

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
	char* data;
	int fsize;
	int position;

	CFile();
	CFile(const char* filepath);
	~CFile();
	void write(const char* filepath, char* nativepath);
	void seek(int off);
	void seekend();
	int read(void* to, int amt);
	int tell();
};

void StripPathExtension(const char* n, char* o);
void StripPath(const char* n, char* o);

#endif