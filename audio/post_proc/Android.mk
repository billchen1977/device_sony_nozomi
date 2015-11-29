ifneq ($(filter msm8974 msm8226 msm8084 msm8992 msm8994,$(TARGET_BOARD_PLATFORM)),)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	bundle.c \
	equalizer.c \
	bass_boost.c \
	virtualizer.c \
	reverb.c \
	effect_api.c

LOCAL_CFLAGS+= -O2 -fvisibility=hidden

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	liblog \
	libtinyalsa

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_RELATIVE_PATH := soundfx
LOCAL_MODULE:= libqcompostprocbundle

LOCAL_C_INCLUDES := \
	external/tinyalsa/include \
	$(call include-path-for, audio-effects)

include $(BUILD_SHARED_LIBRARY)
endif

################################################################################

ifneq ($(filter msm8992 msm8994,$(TARGET_BOARD_PLATFORM)),)

include $(CLEAR_VARS)

LOCAL_CFLAGS := -DLIB_AUDIO_HAL="/system/lib/hw/audio.primary."$(TARGET_BOARD_PLATFORM)".so"

LOCAL_SRC_FILES:= \
	volume_listener.c

LOCAL_CFLAGS+= -O2 -fvisibility=hidden

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	liblog \
	libdl

LOCAL_MODULE_RELATIVE_PATH := soundfx
LOCAL_MODULE:= libvolumelistener

LOCAL_C_INCLUDES := \
	$(call include-path-for, audio-effects)

include $(BUILD_SHARED_LIBRARY)

endif
