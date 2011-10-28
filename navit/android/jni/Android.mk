LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := navit
LOCAL_SRC_FILES := navit.c

# for debug
LOCAL_CFLAGS := -g

include $(BUILD_SHARED_LIBRARY)

