LOCAL_PATH := $(call my-dir)
#
# Transcode AVILib
#
# ...


# ...
#
# Transcode WAVLib
#
# Source files
MY_WAVLIB_SRC_FILES := wavlib.c platform_posix.c
# Include path to export
MY_WAVLIB_C_INCLUDES := $(LOCAL_PATH)
#
# WAVLib static
#
include $(CLEAR_VARS)
# Module name
LOCAL_MODULE := wavlib_static
# Source files
LOCAL_SRC_FILES := $(MY_WAVLIB_SRC_FILES)
# Include path to export
LOCAL_EXPORT_C_INCLUDES := $(MY_WAVLIB_C_INCLUDES)
# Build a static library
include $(BUILD_STATIC_LIBRARY)
#
# WAVLib shared
#
include $(CLEAR_VARS)
# Module name
LOCAL_MODULE := wavlib_shared
# Source files
LOCAL_SRC_FILES := $(MY_WAVLIB_SRC_FILES)
# Include path to export
LOCAL_EXPORT_C_INCLUDES := $(MY_WAVLIB_C_INCLUDES)
# Build a shared library
include $(BUILD_SHARED_LIBRARY)