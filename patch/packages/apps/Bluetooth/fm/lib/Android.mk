LOCAL_PATH := $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	$(call all-subdir-java-files) \
	src/com/broadcom/fm/fmreceiver/IFmReceiverService.aidl \
	src/com/broadcom/fm/fmreceiver/IFmReceiverCallback.aidl

LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_MODULE:= com.broadcom.fm

include $(BUILD_STATIC_JAVA_LIBRARY)
