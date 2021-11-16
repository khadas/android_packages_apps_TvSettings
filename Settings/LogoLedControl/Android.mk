LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := LogoLedControl
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PRIVILEGED_MODULE := true
#LOCAL_JAVA_LIBRARIES := droidlogic
LOCAL_STATIC_JAVA_LIBRARIES := \
	    libserialcontrol

LOCAL_PREBUILT_JNI_LIBS += libs/arm/libserial_port.so

ifeq ($(shell test $(PLATFORM_SDK_VERSION) -ge 28 && echo OK),OK)
LOCAL_PRIVATE_PLATFORM_APIS := true
else
LOCAL_SDK_VERSION := current
endif


include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libserialcontrol:libs/libserialcontrol.aar
include $(BUILD_MULTI_PREBUILT)
