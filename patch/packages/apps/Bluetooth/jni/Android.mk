LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    com_android_bluetooth_btservice_AdapterService.cpp \
    com_android_bluetooth_hfp.cpp \
    com_android_bluetooth_hfpclient.cpp \
    com_android_bluetooth_a2dp.cpp \
    com_android_bluetooth_a2dp_sink.cpp \
    com_android_bluetooth_avrcp.cpp \
    com_android_bluetooth_avrcp_controller.cpp \
    com_android_bluetooth_hid.cpp \
    com_android_bluetooth_hdp.cpp \
    com_android_bluetooth_pan.cpp \
    com_android_bluetooth_gatt.cpp \
    com_android_bluetooth_sdp.cpp

ifeq ($(strip $(BOARD_HAVE_FMRADIO_BCM)),true)
LOCAL_SRC_FILES += ../fm/app/jni/com_broadcom_fm_service.cpp
LOCAL_CFLAGS += -DBOARD_HAVE_FMRADIO_BCM
endif

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libnativehelper \
    libcutils \
    libutils \
    liblog \
    libhardware

LOCAL_MULTILIB := 32

#LOCAL_CFLAGS += -O0 -g

LOCAL_MODULE := libbluetooth_jni
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
