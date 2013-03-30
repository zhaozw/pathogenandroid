LOCAL_PATH := $(call my-dir)  
include $(CLEAR_VARS)  
LOCAL_MODULE    := ndk1
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libzip/ $(LOCAL_PATH)/../libpng/ $(LOCAL_PATH)/../jpeg8d/
LOCAL_STATIC_LIBRARIES := libzip libpng cocos_jpeg_static android_native_app_glue
# libGLESv1
#libavcodec libavformat libavutil libc libz
LOCAL_DEFAULT_CPP_EXTENSION := cpp 
LOCAL_SRC_FILES := utils.cpp shader.cpp font.cpp file.cpp image.cpp 3dmath.cpp quake3bsp.cpp \
					model.cpp weapon.cpp zombie.cpp video.cpp script.cpp sound.cpp \
					skybox.cpp player.cpp decal.cpp billboard.cpp gui.cpp particle.cpp \
					physics.cpp frustum.cpp menu.cpp entity.cpp item.cpp main.cpp
LOCAL_LDLIBS := -ldl -llog -lz -lGLESv2 -landroid -lEGL 
#-lGLESv1_CM
#-lglesv1
include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/native_app_glue)