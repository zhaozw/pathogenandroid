LOCAL_PATH := $(call my-dir)  
include $(CLEAR_VARS)  
LOCAL_MODULE    := ndk1
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../jpeg8d/
LOCAL_STATIC_LIBRARIES := cocos_jpeg_static android_native_app_glue
LOCAL_DEFAULT_CPP_EXTENSION := cpp 
LOCAL_SRC_FILES := file.cpp shader.cpp image.cpp main.cpp
LOCAL_LDLIBS := -ldl -llog -lGLESv2 -landroid -lEGL 
include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/native_app_glue)