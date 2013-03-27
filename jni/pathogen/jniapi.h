


#ifndef JNIAPI_H
#define JNIAPI_H

#include <jni.h>  

extern "C" 
{
    JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnCreate(JNIEnv* jenv, jobject obj);
    JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnResume(JNIEnv* jenv, jobject obj);
    JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnPause(JNIEnv* jenv, jobject obj);
    JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnStop(JNIEnv* jenv, jobject obj);
    //JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeSetSurface(JNIEnv* jenv, jobject obj, jobject surface);
    JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_SurfChang(JNIEnv * env, jobject obj,  jint width, jint height);
    JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_SurfCreat(JNIEnv * env, jobject obj);
    JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_Step(JNIEnv * env, jobject obj);
	JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_nativeInit(JNIEnv * env, jclass cls, jstring apkPath, jstring tmpPath, jobject amgr);
};

#endif