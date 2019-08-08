cmake_minimum_required(VERSION 3.5)

message("SDK = ${FSDK_DIR}")

include_directories(./ "${FSDK_DIR}/include")

add_library( # Sets the name of the library.
        wrapper

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        wrapper_wrap.cxx)

add_library(faceSdkEngine_prebuilt SHARED IMPORTED)
set_property(TARGET faceSdkEngine_prebuilt PROPERTY IMPORTED_LOCATION "${FSDK_DIR}/lib/clang/${ANDROID_ABI}/libFaceEngineSDK.so")

add_library(flower_prebuilt SHARED IMPORTED)
set_property(TARGET flower_prebuilt PROPERTY IMPORTED_LOCATION "${FSDK_DIR}/lib/clang/${ANDROID_ABI}/libflower.so")

find_library(LOG_LIBRARY log)

target_link_libraries(wrapper
        faceSdkEngine_prebuilt
        flower_prebuilt
        ${LOG_LIBRARY}
        )

