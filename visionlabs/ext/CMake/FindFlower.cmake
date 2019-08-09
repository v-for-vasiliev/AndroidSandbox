# Find flower library and includes.
# Sets the usual variables expected for find_package scripts:
# FLOWER_INCLUDE_DIRS - headers location
# FLOWER_LIBRARIES - libraries to link against
# FLOWER_INSTALL_LIBRARIES - libraries to be installed
# FLOWER_DEPENDENCY_LIBRARIES - cudnn & other dependency libraries
# FLOWER_FOUND - true if Face SDK was found.

# This is the directory where flower is located.
set(FLOWER_ROOT "$ENV{FLOWER_ROOT}" CACHE PATH "Flower root directory.")

# Library path suffix depends on compiler and architecture
set(FLOWER_LIB_SUFFIX "lib/")
set(FLOWER_BIN_SUFFIX "bin/")
if(WIN32)
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".lib" ".dll")
elseif(APPLE AND NOT IOS) # mac os
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".a" ".dylib")
else()
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".so" ".a")
endif()

if(IOS)
    set(INCLUDE_PATH ${FLOWER_ROOT})
    set(BIN_PATH ${FLOWER_ROOT}/Frameworks)
	set(LIB_PATH ${FLOWER_ROOT}/Frameworks)
elseif(ANDROID)
    set(INCLUDE_PATH ${FLOWER_ROOT}/include/ NO_CMAKE_FIND_ROOT_PATH)
    set(BIN_PATH ${FLOWER_ROOT}/bin/ NO_CMAKE_FIND_ROOT_PATH)
	set(LIB_PATH ${FLOWER_ROOT}/lib/${ANDROID_ABI}/ NO_CMAKE_FIND_ROOT_PATH)
else()
    set(INCLUDE_PATH ${FLOWER_ROOT}/include/)
    set(BIN_PATH ${FLOWER_ROOT}/bin/)
	set(LIB_PATH ${FLOWER_ROOT}/lib/)
endif()

find_path(FLOWER_INCLUDE_DIRS flower/runtime.h PATHS
    $ENV{FLOWER_ROOT}/include/
    /usr/include/
    ${INCLUDE_PATH}
    )

# iOS framework have different header structure: not flower/header.h, but Headers/header.h. But when you link against framework it's name is used as prefix for includes, so you still use its includes as flower/header.h in c++ code.
# Now the reason to set this variable is that its used for every other platform AND it still contains vlc headers not included in framework
if(IOS)
    set(FLOWER_INCLUDE_DIRS ${FLOWER_ROOT}/include)
endif()
	
find_library(FLOWER_LIBRARIES_REL NAMES flower
    PATHS
    $ENV{FLOWER_ROOT}/lib/
    $ENV{FLOWER_ROOT}/bin/
    ${LIB_PATH}
    ${BIN_PATH}
    )

find_library(FLOWER_LIBRARIES_DBG NAMES flowerd
    PATHS
    $ENV{FLOWER_ROOT}/lib/
    $ENV{FLOWER_ROOT}/bin/
    ${LIB_PATH}
    ${BIN_PATH}
    )

#emscripten *.bc-files with cmake:
if(EMSCRIPTEN)
	if(EXISTS "${FLOWER_ROOT}/include/flower/runtime.h")
		message("emscripten: ${FLOWER_ROOT}/include/flower/runtime.h exists.")
		SET(FLOWER_INCLUDE_DIRS "${FLOWER_ROOT}/include/")
		SET(FLOWER_INCLUDE_DIR "${FLOWER_ROOT}/include/")
	endif()
		message("FLOWER_INCLUDE_DIRS:${FLOWER_INCLUDE_DIRS}")

	if(EXISTS "${FLOWER_ROOT}/lib/libflower.bc")
		message("emscripten: ${FLOWER_ROOT}/lib/libflower.bc exists.")
		SET(FLOWER_LIBRARIES_REL "${FLOWER_ROOT}/lib/libflower.bc")
	else()
		message(ERROR "emscripten: ${FLOWER_ROOT}/lib/libflower.bc NOT exists.")
	endif()

	if(EXISTS "${FLOWER_ROOT}/lib/libflowerd.bc")
		message("emscripten: ${FLOWER_ROOT}/lib/libflowerd.bc exists.")
		SET(FLOWER_LIBRARIES_DBG "${FLOWER_ROOT}/lib/libflowerd.bc")
	endif()
endif()

if(WIN32)
	find_library(FLOWER_LIBRARIES_DLL_REL
		NAMES flower
		PATHS
		$ENV{FLOWER_ROOT}/bin/
		${FLOWER_ROOT}/bin/
		PATH_SUFFIXES ${FLOWER_BIN_SUFFIX})
	find_library(FLOWER_LIBRARIES_DLL_DBG
		NAMES flowerd		
		PATHS
		$ENV{FLOWER_ROOT}/bin/
		${FLOWER_ROOT}/bin/
		PATH_SUFFIXES ${FLOWER_BIN_SUFFIX})
	#set(CNN_DLL ${CNN_DLL_RELEASE} ${CNN_DLL_DEBUG})
endif()

message("FLOWER LIBRARIES DBG: ${FLOWER_LIBRARIES_DBG}.")
message("FLOWER LIBRARIES REL: ${FLOWER_LIBRARIES_REL}.")

if(FLOWER_LIBRARIES_DBG)
    set(FLOWER_LIBRARIES optimized ${FLOWER_LIBRARIES_REL} debug ${FLOWER_LIBRARIES_DBG})
 else()
    set(FLOWER_LIBRARIES ${FLOWER_LIBRARIES_REL})
endif()

message("FLOWER LIBRARIES: ${FLOWER_LIBRARIES}.")

if(WIN32)
	list(APPEND FLOWER_INSTALL_LIBRARIES ${FLOWER_LIBRARIES_DLL_REL})
	if(FLOWER_LIBRARIES_DLL_DBG)
		list(APPEND FLOWER_INSTALL_LIBRARIES ${FLOWER_LIBRARIES_DLL_DBG})
	endif()	
else()
	set(FLOWER_INSTALL_LIBRARIES ${FLOWER_LIBRARIES})
endif()

message("FLOWER DEPENDENCY LIBRARIES: ${FLOWER_DEPENDENCY_LIBRARIES}.")

# Support the REQUIRED and QUIET arguments, and set FLOWER_FOUND if found.
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(
    FLOWER DEFAULT_MSG
    FLOWER_LIBRARIES
    FLOWER_INCLUDE_DIRS
	FLOWER_INSTALL_LIBRARIES
    )

mark_as_advanced(
    FLOWER_LIBRARIES_REL
    FLOWER_LIBRARIES_DBG
    )
