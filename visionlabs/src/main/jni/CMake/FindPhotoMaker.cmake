# Find the Vision Labs PhotoMaker library.
# Sets the usual variables expected for find_package scripts:
# PHOTOMAKER_INCLUDE_DIRS - headers location
# PHOTOMAKER_LIB - photomaker lib path
# PHOTOMAKER_FOUND - true if Face SDK was found.

# This is the directory where the Face SDK is located.
# By default PHOTOMAKERDIR environment variable value is taken.
set(PHOTOMAKER_ROOT "$ENV{PHOTOMAKERDIR}" CACHE PATH "Vision Labs photo maker root directory.")
set(PHOTOMAKER_INCLUDE_DIRS ${PHOTOMAKER_ROOT}/include)

# Determine compiler version and architecture.
# ios has no architechture/compiler branching, because only can only use clang
# and if you need multiple architechtures there still compiled into single universal binary
if(NOT IOS)
	if(MSVC10)
		set(FSDK_COMPILER_NAME vs2010)
	elseif(MSVC11)
		set(FSDK_COMPILER_NAME vs2012)
	elseif(MSVC12)
		set(FSDK_COMPILER_NAME vs2013)
	elseif(MSVC14)
		set(FSDK_COMPILER_NAME vs2015)
	elseif(CMAKE_COMPILER_IS_GNUCXX)
		set(FSDK_COMPILER_NAME gcc4)
	elseif("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
		set(FSDK_COMPILER_NAME clang)
	elseif("${CMAKE_CXX_COMPILER_ID}" STREQUAL "AppleClang")
		set(FSDK_COMPILER_NAME clang)
	else()
		message(SEND_ERROR "Unsupported compiler: ${FSDK_COMPILER_NAME}")
	endif()

endif()

set(PHOTOMAKER_LIB "${PHOTOMAKER_ROOT}/lib/${FSDK_COMPILER_NAME}/${ANDROID_ABI}/libPhotoMaker.so")

if (EXISTS ${PHOTOMAKER_LIB})
    message("PHOTOMAKER_LIB found!")
    set(PHOTOMAKER_FOUND ON)
else()
    message("PHOTOMAKER_LIB NOT found!")
    message("at path PHOTOMAKER_LIB:${PHOTOMAKER_LIB}")
    set(PHOTOMAKER_FOUND OFF)
endif()