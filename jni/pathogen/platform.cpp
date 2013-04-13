

#include "logger.h"
#include "platform.h"
#include "main.h"
#include "file.h"

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
void plat_free(void *ptr)
{
    free(ptr);
}

int plat_read(CFile* file, void* buff, int len)
{
	return file->read(buff, len);
}

int plat_seek(CFile* file, int off, int whence)
{
	return file->seek(off, whence);
}

//int plat_ftruncate(int fd, int64_t length)
int plat_ftruncate(CFile* file, int64_t length)
{
    //return ftruncate(fd, length);
	return 0;
}

void plat_close(CFile* file)
{
	file->close();
}

//void* plat_malloc(const char *file, int line, size_t size)
void* plat_malloc(size_t size)
{//__FILE__, __LINE__
    return malloc(size);
}

void* plat_zalloc(size_t size)
{
    return calloc(1, size);
}

void* plat_realloc(void *ptr, size_t size)
{
    return realloc(ptr, size);
}

/* 
 * automatically restart after a recoverable interruption
 */
//ssize_t plat_write(int fd, const void *buf, size_t count)
ssize_t plat_write(CFile* file, const void *buf, size_t count)
{
	/*
    ssize_t n = 0, r = 0;

    while (r < count) {
        n = write(fd, buf + r, count - r);
        if (n < 0)
            return n;

        r += n;
    }
    return r;*/
}

int plat_log_send(PlatLogLevel level,
                  const char *tag, const char *fmt, ...)
{
    char buffer[1024];
    va_list ap;

    va_start(ap, fmt);
    vsnprintf(buffer, 1024, fmt, ap);
    va_end(ap);

    //fprintf(stderr, "[%s] %s\n", tag, buffer);
	LOGE("[%s] %s\n", tag, buffer);

    return 0;
}