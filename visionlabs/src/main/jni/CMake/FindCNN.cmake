# Find the VisionLabs CNN SDK.
# Sets the usual variables expected for find_package scripts:
# CNN_INCLUDE_DIR - header location
# CNN_LIBRARIES - library to link against
# CNN_FOUND - true if CNN SDK was found.
# CNN_DEPENDENCIES - list of dependened libs, with should be installed with CNN
# CNN_INSTALL_LIBRARIES - list lib files to be installed

set(CNN_HOME CACHE PATH "Path to VisionLabs CNN SDK root folder")
option(CNN_WITH_MKL ON "add mkl lib to dependencies")

include(GetSymLinkChain)
include(Filter)

if(MSVC10)
	set(CNN_COMPILER_NAME vs2010)
elseif(MSVC11)
	set(CNN_COMPILER_NAME vs2012)
elseif(MSVC12)
	set(CNN_COMPILER_NAME vs2013)
elseif(MSVC14)
	set(CNN_COMPILER_NAME vs2015)
elseif(CMAKE_COMPILER_IS_GNUCXX)
	set(CNN_COMPILER_NAME gcc4)
else()
	message(SEND_ERROR "Unsupported compiler")
endif()

if(CMAKE_SIZEOF_VOID_P EQUAL 8)
	set(CNN_TARGET_NAME x64)
else()
	set(CNN_TARGET_NAME x86)
endif()

# Library path suffix depends on compiler and architecture
set(CNN_LIB_SUFFIX "lib/${CNN_COMPILER_NAME}/${CNN_TARGET_NAME}")
set(CNN_BIN_SUFFIX "bin/${CNN_COMPILER_NAME}/${CNN_TARGET_NAME}")
if(WIN32)
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".lib" ".dll")
else()
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".so" ".a")
endif()

find_library(CNN_LIB_DEBUG
	NAMES cnnd
	HINTS $ENV{CNN_DIR}
	PATHS ${CNN_HOME}
	PATH_SUFFIXES ${CNN_LIB_SUFFIX})
#get_filename_component(CNN_LIB_DEBUG ${CNN_LIB_DEBUG} REALPATH)

set(CNN_LIB_DEBUG_LIST)
if(CNN_LIB_DEBUG)
	getSymLinkChain(${CNN_LIB_DEBUG} CNN_LIB_DEBUG_LIST)
endif()
find_library(CNN_LIB_RELEASE
	NAMES cnn
	HINTS $ENV{CNN_DIR}
	PATHS ${CNN_HOME}
	PATH_SUFFIXES ${CNN_LIB_SUFFIX})

set(CNN_LIB_RELEASE_LIST)
if(CNN_LIB_RELEASE)
	getSymLinkChain(${CNN_LIB_RELEASE} CNN_LIB_RELEASE_LIST)
endif()

find_path(CNN_INCLUDE_DIR
	NAMES "NeuralNetwork.h"
	HINTS $ENV{CNN_DIR}
	PATHS ${CNN_HOME}
	PATH_SUFFIXES include)

# ==============
find_library(CNN_VLF_LIB_DEBUG
	NAMES vlfd
	HINTS $ENV{CNN_DIR}
	PATHS ${CNN_HOME}
	PATH_SUFFIXES ${CNN_LIB_SUFFIX})
find_library(CNN_VLF_LIB_RELEASE
	NAMES vlf
	HINTS $ENV{CNN_DIR}
	PATHS ${CNN_HOME}
	PATH_SUFFIXES ${CNN_LIB_SUFFIX})
set(CNN_DEPENDENCIES ${CNN_VLF_LIB_RELEASE})
if(${CNN_VLF_LIB_DEBUG})
       set(CNN_DEPENDENCIES "${CNN_DEPENDENCIES} ${CNN_VLF_LIB_DEBUG}")
endif()

if(CNN_WITH_MKL)
	find_library(CNN_MKL_LIB_iomp5
		NAMES iomp5
		HINTS $ENV{CNN_DIR}
		PATHS ${CNN_HOME}
		PATH_SUFFIXES ${CNN_LIB_SUFFIX})
	set(CNN_DEPENDENCIES ${CNN_DEPENDENCIES} ${CNN_MKL_LIB_iomp5})
endif()

#install .dll for Windows
if(WIN32)
	find_library(CNN_DLL_RELEASE
		NAMES cnn
		HINTS $ENV{CNN_DIR}
		PATHS ${CNN_HOME}
		PATH_SUFFIXES ${CNN_BIN_SUFFIX})
	find_library(CNN_DLL_DEBUG
		NAMES cnnd
		HINTS $ENV{CNN_DIR}
		PATHS ${CNN_HOME}
		PATH_SUFFIXES ${CNN_BIN_SUFFIX})
	#set(CNN_DLL ${CNN_DLL_RELEASE} ${CNN_DLL_DEBUG})
endif()

#filtring static libraries from dependencies
set(FILTERED_CNN_DEPENDENCIES)
if(UNIX)
	filter(${CNN_DEPENDENCIES} ".+\\.so$" FILTERED_CNN_DEPENDENCIES)
else() #Windows
	filter(${CNN_DEPENDENCIES} ".+\\.dll$" FILTERED_CNN_DEPENDENCIES)
endif()

#set(CNN_DEPENDENCIES ${FILTERED_CNN_DEPENDENCIES} ${CNN_DLL_RELEASE} ${CNN_DLL_DEBUG})
#list(APPEND CNN_INSTALL_LIBRARIES ${CNN_LIB_DEBUG_LIST} ${CNN_LIB_RELEASE_LIST})

if(CNN_DLL_RELEASE)
  set(CNN_DEPENDENCIES ${FILTERED_CNN_DEPENDENCIES} ${CNN_DLL_RELEASE})
  list(APPEND CNN_INSTALL_LIBRARIES ${CNN_LIB_RELEASE_LIST})
elseif(CNN_DLL_DEBUG)
  set(CNN_DEPENDENCIES ${FILTERED_CNN_DEPENDENCIES} ${CNN_DLL_DEBUG})
  list(APPEND CNN_INSTALL_LIBRARIES ${CNN_LIB_DEBUG_LIST})
else()
  set(CNN_DEPENDENCIES ${FILTERED_CNN_DEPENDENCIES} ${CNN_DLL_RELEASE} ${CNN_DLL_DEBUG})
  list(APPEND CNN_INSTALL_LIBRARIES ${CNN_LIB_DEBUG_LIST} ${CNN_LIB_RELEASE_LIST})
endif()

# Support the REQUIRED and QUIET arguments, and set CNN_FOUND if found.
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(
	"CNN" DEFAULT_MSG
	#CNN_LIB_DEBUG
	#CNN_LIB_RELEASE
	CNN_INCLUDE_DIR)

set(CNN_LIBRARIES)
if(CNN_FOUND)
	if(CNN_LIB_RELEASE)
		foreach(LIB ${CNN_LIB_RELEASE})
			list(APPEND CNN_LIBRARIES optimized ${LIB}) 
		endforeach()
	endif()
	if(CNN_LIB_DEBUG)
		foreach(LIB ${CNN_LIB_DEBUG})
			list(APPEND CNN_LIBRARIES debug ${LIB}) 
		endforeach()
	endif()	
	#set(CNN_LIBRARIES debug ${CNN_LIB_DEBUG} optimized ${CNN_LIB_RELEASE})
    message(STATUS "CNN SDK include = ${CNN_INCLUDE_DIR}")
    message(STATUS "CNN SDK libraries = ${CNN_LIBRARIES}")
	message(STATUS "CNN SDK dependencies = ${CNN_DEPENDENCIES}")
	message(STATUS "CNN SDK libs to be installed = ${CNN_INSTALL_LIBRARIES}")
else()
    message(STATUS "No CNN SDK found")
endif()

# Don't show in GUI
mark_as_advanced(
  CNN_COMPILER_NAME
  CNN_TARGET_NAME
  CNN_LIB_SUFFIX
  CNN_LIB_RELEASE
  CNN_LIB_DEBUG
  CNN_VLF_LIB_DEBUG
  CNN_VLF_LIB_RELEASE
  CNN_MKL_LIB_iomp5)
