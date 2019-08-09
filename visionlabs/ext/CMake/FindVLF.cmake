# Find the VisionLabs VLF SDK.
# Sets the usual variables expected for find_package scripts:
# VLF_INCLUDE_DIR - header location
# VLF_LIBRARIES - library to link against
# VLF_FOUND - true if VLF SDK was found.

set(VLF_HOME CACHE PATH "Path to VisionLabs VLF SDK root folder")

if(MSVC10)
	set(VLF_COMPILER_NAME vs2010)
elseif(MSVC11)
	set(VLF_COMPILER_NAME vs2012)
elseif(MSVC12)
	set(VLF_COMPILER_NAME vs2013)
elseif(MSVC14)
	set(VLF_COMPILER_NAME vs2015)
elseif(CMAKE_COMPILER_IS_GNUCXX)
	set(VLF_COMPILER_NAME gcc4)
else()
	message(SEND_ERROR "Unsupported compiler")
endif()

if(CMAKE_SIZEOF_VOID_P EQUAL 8)
	set(VLF_TARGET_NAME x64)
else()
	set(VLF_TARGET_NAME x86)
endif()

# Library path suffix depends on compiler and architecture
set(VLF_LIB_SUFFIX "lib/${VLF_COMPILER_NAME}/${VLF_TARGET_NAME}")

find_library(VLF_LIB_DEBUG
	NAMES vlfd
	HINTS $ENV{VLF_DIR}
	PATHS ${VLF_HOME}
	PATH_SUFFIXES ${VLF_LIB_SUFFIX})

find_library(VLF_LIB_RELEASE
	NAMES vlf
	HINTS $ENV{VLF_DIR}
	PATHS ${VLF_HOME}
	PATH_SUFFIXES ${VLF_LIB_SUFFIX})

find_path(VLF_INCLUDE_DIR
	NAMES vlf/Log.h
	HINTS $ENV{VLF_DIR}
	PATHS ${VLF_HOME}
	PATH_SUFFIXES include)

# Support the REQUIRED and QUIET arguments, and set VLF_FOUND if found.
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(
	"VLF" DEFAULT_MSG
	VLF_LIB_DEBUG
	VLF_LIB_RELEASE
	VLF_INCLUDE_DIR)

if(VLF_FOUND)
	set(VLF_LIBRARIES debug ${VLF_LIB_DEBUG} optimized ${VLF_LIB_RELEASE})
    message(STATUS "VLF SDK include = ${VLF_INCLUDE_DIR}")
    message(STATUS "VLF SDK libraries = ${VLF_LIBRARIES}")
else()
    message(STATUS "No VLF SDK found")
endif()

# Don't show in GUI
mark_as_advanced(
  VLF_COMPILER_NAME
  VLF_TARGET_NAME
  VLF_LIB_SUFFIX
  VLF_LIB_RELEASE
  VLF_LIB_DEBUG)
