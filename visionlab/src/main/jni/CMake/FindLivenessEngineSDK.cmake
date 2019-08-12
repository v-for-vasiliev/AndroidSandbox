# Find the Vision Labs LivenessEngine SDK.
# Sets the usual variables expected for find_package scripts:
# LIVENESS_INCLUDE_DIRS - headers location
# LIVENESS_LIB - liveness library to link
# LIVENESS_FOUND - true if Face SDK was found.



set(LIVENESS_ROOT "$ENV{LIVENESSDIR}" CACHE PATH "Vision Labs Liveness engine SDK root dir.")
set(LIVENESS_INCLUDE_DIRS "${LIVENESS_ROOT}/include")

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

set(LIVENESS_LIB "${LIVENESS_ROOT}/lib/${FSDK_COMPILER_NAME}/${ANDROID_ABI}/libLivenessEngineSDK.so")

if(EXISTS ${LIVENESS_INCLUDE_DIRS})
message(STATUS "LIVENESS_INCLUDE_DIRS = ${LIVENESS_INCLUDE_DIRS}")
	if(EXISTS "${LIVENESS_LIB}")
		message("LIVENESS_LIB is found at ${LIVENESS_LIB}")
		set(LIVENESS_FOUND ON)
	else()
		message(STATUS "LIVENESS_LIB could not be found!")
	endif()
else()
message(STATUS "LIVENESS_INCLUDE_DIRS could not be found!")
endif()
