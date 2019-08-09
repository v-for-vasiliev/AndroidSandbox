# Find the VisionLabs Core (vlc)
# Sets the usual variables expected for find_package scripts:
# VLC_INCLUDE_DIR - header location
# VLC_LIBRARIES - library to link against
# VLC_FOUND - true if vlc was found.

find_path(VLC_INCLUDE_DIR
	NAMES vlc/config.h
	HINTS $ENV{VLC_ROOT}
	PATHS ${VLC_ROOT}
	PATH_SUFFIXES include)

find_library(VLC_LIBRARY_DEBUG
	NAMES vlcd
	HINTS $ENV{VLC_ROOT}
	PATHS ${VLF_ROOT}
	PATH_SUFFIXES lib)

find_library(VLC_LIBRARY_RELEASE
	NAMES vlc
	HINTS $ENV{VLC_ROOT}
	PATHS ${VLF_ROOT}
	PATH_SUFFIXES lib)

if (VLC_LIBRARY_DEBUG AND VLC_LIBRARY_RELEASE)
	set(VLC_LIBRARIES debug ${VLC_LIBRARY_DEBUG} optimized ${VLC_LIBRARY_RELEASE})
elseif (VLC_LIBRARY_DEBUG)
	set(VLC_LIBRARIES ${VLC_LIBRARY_DEBUG})
elseif (VLC_LIBRARY_RELEASE)
	set(VLC_LIBRARIES ${VLC_LIBRARY_RELEASE})
endif ()

# Support the REQUIRED and QUIET arguments, and set VLC_FOUND if found.
include(FindPackageHandleStandardArgs)

find_package_handle_standard_args(
	"VLC" DEFAULT_MSG
	VLC_INCLUDE_DIR
	VLC_LIBRARIES
	)

if (VLC_LIBRARY_DEBUG)
	# Don't show in GUI
	mark_as_advanced(
  		VLC_LIBRARY_DEBUG)
endif ()

if (VLC_LIBRARY_RELEASE)
	# Don't show in GUI
	mark_as_advanced(
		VLC_LIBRARY_RELEASE)
endif ()
