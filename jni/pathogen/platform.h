
#ifndef PLATFORM_H
#define PLATFORM_H

#include "file.h"

///////////////////////// platform ///////////////////////////////////
/*
//void plat_free(void* d)
void plat_free(wav_* d)
{
	free(d);
	//delete d;
}

void plat_free(avi_t* d)
{
	free(d);
	//delete d;
}

void plat_free(char* d)
{
	free(d);
	//delete [] d;
}
*/
void plat_free(void *ptr);

int plat_read(CFile* file, void* buff, int len);

int plat_seek(CFile* file, int off, int whence);

//int plat_ftruncate(int fd, int64_t length)
int plat_ftruncate(CFile* file, int64_t length);

void plat_close(CFile* file);

//void* plat_malloc(const char *file, int line, size_t size)
void* plat_malloc(size_t size);

void* plat_zalloc(size_t size);
void* plat_realloc(void *ptr, size_t size);

/* 
 * automatically restart after a recoverable interruption
 */
//ssize_t plat_write(int fd, const void *buf, size_t count)
ssize_t plat_write(CFile* file, const void *buf, size_t count);

enum platloglevel_ {
    PLAT_LOG_DEBUG = 0,
    PLAT_LOG_INFO,
    PLAT_LOG_WARNING,
    PLAT_LOG_ERROR,
};
typedef enum platloglevel_ PlatLogLevel;

int plat_log_send(PlatLogLevel level,
                  const char *tag, const char *fmt, ...);

//////////////////////////////////////////////////////////////////////

#endif