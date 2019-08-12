# Find the Sentinel HASP SDK.
# Sets the usual variables expected for find_package scripts:
# HASP_INCLUDE_DIRS - headers location
# HASP_LIBRARIES - libraries to link against
# HASP_FOUND - true if Face SDK was found.

# Sentinel HASP personalized key.
set(HASP_KEY CACHE STRING
	"Sentinel HASP personalized key (e.g.: '123456' or 'demo').")

# This is the directory where the HASP API files are located.
set(HASP_API CACHE PATH
	"Sentinel HASP SDK API directory (e.g.: '$HOME$/SafeNet/Sentinel LDK 6.4/API').")

# This is the directory where the HASP C runtime API files are located.
set(HASP_RUNTIME "${HASP_API}/Runtime/C")


# Target and library name.
if(WIN32)
	# Windows
	if(CMAKE_SIZEOF_VOID_P EQUAL 8)
		set(HASP_TARGET_NAME x64)
		set(HASP_LIB_NAME libhasp_windows_x64_${HASP_KEY})
	else()
		set(HASP_TARGET_NAME win32)
		set(HASP_LIB_NAME libhasp_windows_${HASP_KEY})
	endif()	
elseif(UNIX)
	# Linux, Apple
	if(APPLE)
		# There is no difference under OSX.
		set(HASP_TARGET_NAME)
		set(HASP_LIB_NAME hasp_darwin_${HASP_KEY})
	else()
		# Assume Linux.
		if(CMAKE_SIZEOF_VOID_P EQUAL 8)
			set(HASP_TARGET_NAME x86_64)
			set(HASP_LIB_NAME hasp_linux_x86_64_${HASP_KEY})
		else()		
			set(HASP_TARGET_NAME x86)
			set(HASP_LIB_NAME hasp_linux_${HASP_KEY})
		endif()	
	endif()
endif()


# Look for headers.
find_path(HASP_INCLUDE_DIRS
          NAMES hasp_api.h
          PATHS ${HASP_RUNTIME}
          PATH_SUFFIXES ${HASP_TARGET_NAME})

#message(STATUS "HASP [DEBUG]: HASP_INCLUDE_DIRS = ${HASP_INCLUDE_DIRS}.")


# Find libraries.
find_library(HASP_LIBRARIES
			 NAMES ${HASP_LIB_NAME}
			 PATHS ${HASP_RUNTIME}
			 PATH_SUFFIXES ${HASP_TARGET_NAME})
				 
#message(STATUS "HASP [DEBUG]: HASP_LIBRARIES = ${HASP_LIBRARIES}.")


# Support the REQUIRED and QUIET arguments, and set HASP_FOUND if found.
include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(HASP DEFAULT_MSG 
                                  HASP_LIBRARIES
                                  HASP_INCLUDE_DIRS)


if(HASP_FOUND)
	message(STATUS "HASP [INFO]: Found HASP in ${HASP_API}.")
else()
	message(STATUS "HASP [WARN]: HASP was NOT found.")
	
	# Reset these (just in case).
	set(HASP_LIBRARIES)
	set(HASP_INCLUDE_DIRS)
endif(HASP_FOUND)

# Don't show in GUI
mark_as_advanced(
	HASP_INCLUDE_DIRS
	HASP_LIBRARIES
	HASP_TARGET_NAME
	HASP_LIB_NAME
	HASP_RUNTIME
)
