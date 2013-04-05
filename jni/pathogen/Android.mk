LOCAL_PATH := $(call my-dir)  
include $(CLEAR_VARS)  
LOCAL_MODULE    := ndk1
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libzip/ $(LOCAL_PATH)/../libpng/ $(LOCAL_PATH)/../jpeg8d/
LOCAL_STATIC_LIBRARIES := libzip libpng cocos_jpeg_static android_native_app_glue
# libzip 
# libGLESv1
#libavcodec libavformat libavutil libc libz
LOCAL_DEFAULT_CPP_EXTENSION := cpp 
LOCAL_SRC_FILES := utils.cpp shader.cpp font.cpp file.cpp image.cpp 3dmath.cpp \
					model.cpp script.cpp sound.cpp skybox.cpp gui.cpp item.cpp \
					entity.cpp physics.cpp particle.cpp billboard.cpp decal.cpp \
					player.cpp video.cpp zombie.cpp weapon.cpp quake3bsp.cpp \
					frustum.cpp menu.cpp main.cpp
# http://stackoverflow.com/questions/13108603/ndk-error-trying-to-build-cyanogenmod-camera-application
# LOCAL_LDLIBS += C:/Users/polyfrag/Desktop/android-ndk-r8e/platforms/android-14/arch-arm/usr/lib
LOCAL_LDLIBS := -ldl -lz -llog -lGLESv2 -landroid -lEGL 
# -lz
#-lGLESv1_CM
#-lglesv1
include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/native_app_glue)