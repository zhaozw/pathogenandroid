LOCAL_PATH := $(call my-dir)  
include $(CLEAR_VARS)  
LOCAL_MODULE    := ndk1
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libzip/ $(LOCAL_PATH)/../libpng/ $(LOCAL_PATH)/../jpeg8d/
LOCAL_STATIC_LIBRARIES := libzip libpng cocos_jpeg_static
LOCAL_DEFAULT_CPP_EXTENSION := cpp 
LOCAL_SRC_FILES := utils.cpp shader.cpp font.cpp file.cpp image.cpp 3dmath.cpp quake3bsp.h \
					model.cpp weapon.cpp main.cpp
LOCAL_LDLIBS := -ldl -llog -lz -lGLESv2 -landroid
include $(BUILD_SHARED_LIBRARY)