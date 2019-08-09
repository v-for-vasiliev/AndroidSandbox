# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
include_directories(./ ./include)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${PROJECT_SOURCE_DIR}/CMake/")

find_package(FaceEngineSDK REQUIRED)
find_package(PhotoMaker REQUIRED)
find_package(LivenessEngineSDK REQUIRED)
message(STATUS "LIBS ${FSDK_LIB}")
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
add_library( # Sets the name of the library.
        wrapper

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        wrapper_wrap.cxx)

add_definitions(-DWITH_LIVENESS)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

add_library(faceSdkEngine_prebuilt SHARED IMPORTED)
set_property(TARGET faceSdkEngine_prebuilt PROPERTY IMPORTED_LOCATION "${FSDK_ROOT}/lib/clang/${ANDROID_ABI}/libFaceEngineSDK.so")

add_library(flower_prebuilt SHARED IMPORTED)
set_property(TARGET flower_prebuilt PROPERTY IMPORTED_LOCATION "${FSDK_ROOT}/lib/clang/${ANDROID_ABI}/libflower.so")

add_library(photoMaker_prebuilt SHARED IMPORTED)
set_property(TARGET photoMaker_prebuilt PROPERTY IMPORTED_LOCATION "${PHOTOMAKER_LIB}")

add_library(livenessSdkEngine_prebuilt SHARED IMPORTED)
set_property(TARGET livenessSdkEngine_prebuilt PROPERTY IMPORTED_LOCATION "${FSDK_ROOT}/lib/clang/${ANDROID_ABI}/libLivenessEngineSDK.so")

find_library(loglib log PATH "${ANDROID_NDK}/platforms/${ANDROID_PLATFORM}/arch-${ANDROID_SYSROOT_ABI}/usr/lib/")
include_directories(${PHOTOMAKER_INCLUDE_DIRS} ${FSDK_INCLUDE_DIRS} ${LIVENESS_INCLUDE_DIRS})

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        wrapper
        faceSdkEngine_prebuilt
        flower_prebuilt
        photoMaker_prebuilt
        livenessSdkEngine_prebuilt
        ${loglib})
