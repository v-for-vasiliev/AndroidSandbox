# Find the VisionLabs AppFoundation SDK.
# Sets the usual variables expected for find_package scripts:
# APPFOUNDATION_INCLUDE_DIR - header location
# APPFOUNDATION_LIBRARIES - library to link against
# APPFOUNDATION_FOUND - true if AppFoundation SDK was found.

set(APPFOUNDATION_HOME CACHE PATH "Path to VisionLabs AppFoundation SDK root folder")

# Files applicable to various components
set(APPFOUNDATION_LIB_NAMES
	"VideoGui" 
	"Video" 
	"DatabaseGui" 
	"Database" 
	"ImageViewerGui" 
	"TimelineGui" 
	"Foundation" 
	"Featureextractor"
	"Featureimporter")

set(APPFOUNDATION_INCLUDES
	"vlcplayer.h"			# Video
	"vlcplayerwidget.h"		# VideoGui
	"database.h"			# Database
	"databasemodel.h"		# DatabaseGui
	"imageviewerwidget.h"		# ImageViewerGui
	"timelineviewer.h"		# TimelineGui
	"commandline.h")		# Foundation

if(MSVC10)
  set(APPFOUNDATION_COMPILER_NAME vs2010)
elseif(MSVC11)
  set(APPFOUNDATION_COMPILER_NAME vs2012)
elseif(MSVC12)
  set(APPFOUNDATION_COMPILER_NAME vs2013)
elseif(CMAKE_COMPILER_IS_GNUCXX)
  set(APPFOUNDATION_COMPILER_NAME gcc4)
else()
   message(SEND_ERROR "Unsupported compiler")
endif()

if(CMAKE_SIZEOF_VOID_P EQUAL 8)
  set(APPFOUNDATION_TARGET_NAME x64)
else()
  set(APPFOUNDATION_TARGET_NAME x86)
endif()

# Library path suffix depends on compiler and architecture
set(APPFOUNDATION_LIB_SUFFIX "lib/${APPFOUNDATION_COMPILER_NAME}/${APPFOUNDATION_TARGET_NAME}")

foreach(LIB_NAME ${APPFOUNDATION_LIB_NAMES})
  set(LIB_DEBUG "${LIB_NAME}-NOTFOUND")
  set(LIB_RELEASE "${LIB_NAME}-NOTFOUND")

  find_library(LIB_DEBUG
             NAMES ${LIB_NAME}d
             HINTS $ENV{APPFOUNDATION_DIR}
             PATHS ${APPFOUNDATION_HOME}
             PATH_SUFFIXES ${APPFOUNDATION_LIB_SUFFIX})

  find_library(LIB_RELEASE
             NAMES ${LIB_NAME}
             HINTS $ENV{APPFOUNDATION_DIR}
             PATHS ${APPFOUNDATION_HOME}
             PATH_SUFFIXES ${APPFOUNDATION_LIB_SUFFIX})

  list(APPEND APPFOUNDATION_LIB_DEBUG ${LIB_DEBUG})
  list(APPEND APPFOUNDATION_LIB_RELEASE ${LIB_RELEASE})
endforeach()

find_path(APPFOUNDATION_INCLUDE_DIR
          NAMES ${APPFOUNDATION_INCLUDES}
          HINTS $ENV{APPFOUNDATION_DIR}
          PATHS ${APPFOUNDATION_HOME}
          PATH_SUFFIXES include)

# Support the REQUIRED and QUIET arguments, and set AppFoundation_FOUND if found.
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args("AppFoundation" DEFAULT_MSG 
                                  APPFOUNDATION_LIB_RELEASE
                                  APPFOUNDATION_INCLUDE_DIR)

set(APPFOUNDATION_LIBRARIES)
if(APPFOUNDATION_FOUND)
	if(APPFOUNDATION_LIB_RELEASE)
		foreach(LIB ${APPFOUNDATION_LIB_RELEASE})
			list(APPEND APPFOUNDATION_LIBRARIES optimized ${LIB})
		endforeach()
	endif()
	if(APPFOUNDATION_LIB_DEBUG)
		foreach(LIB ${APPFOUNDATION_LIB_DEBUG})
			list(APPEND APPFOUNDATION_LIBRARIES debug ${LIB})
		endforeach()
		message(STATUS "AppFoundation debug libraries are available.")
	elseif(APPFOUNDATION_LIB_RELEASE)
		foreach(LIB ${APPFOUNDATION_LIB_RELEASE})
			list(APPEND APPFOUNDATION_LIBRARIES debug ${LIB})
		endforeach()
		message(STATUS "AppFoundation debug libraries are NOT available.")
	endif()
	
	message(STATUS "AppFoundation SDK found in ${APPFOUNDATION_HOME}.")
	
	set(APPFOUNDATION_LIB_RELEASE)
    set(APPFOUNDATION_LIB_DEBUG)
else()
	message(STATUS "No AppFoundation SDK found")
endif(APPFOUNDATION_FOUND)

# Don't show in GUI
mark_as_advanced(
  LIB_DEBUG
  LIB_RELEASE
  APPFOUNDATION_INCLUDE_DIR
  APPFOUNDATION_LIB_NAMES
  APPFOUNDATION_INCLUDES
  APPFOUNDATION_COMPILER_NAME
  APPFOUNDATION_TARGET_NAME
  APPFOUNDATION_LIB_SUFFIX
  APPFOUNDATION_LIB_RELEASE
  APPFOUNDATION_LIB_DEBUG)
