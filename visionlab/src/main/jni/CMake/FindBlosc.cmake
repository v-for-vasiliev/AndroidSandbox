# Input:
#  BLOSC_ROOT - path to root directory. Also it's accepted as environment variable
#  BLOSC_LINK_DYNAMIC - link dynamic library instead of static one
# Output:
#  BLOSC_FOUND
#  BLOSC_INCLUDE_DIR
#  BLOSC_static_LIBRARY
#  BLOSC_dynamic_LIBRARY
#  BLOSC_LIBRARIES=if BLOSC_LINK_DYNAMIC then BLOSC_dynamic_LIBRARY else BLOSC_static_LIBRARY

include(FindPackageHandleStandardArgs)

find_path(BLOSC_INCLUDE_DIR "blosc.h"
    PATHS
    ${BLOSC_ROOT} $ENV{BLOSC_ROOT}
    /usr/
    /usr/local/
    PATH_SUFFIXES "include"
    DOC "Path to directory with blosc header")

find_library(BLOSC_static_LIBRARY "libblosc"
    PATHS
    ${BLOSC_ROOT} $ENV{BLOSC_ROOT}
    /usr/
    /usr/local/
    PATH_SUFFIXES "lib" "lib/x64" DOC
    "Path to static library")

find_library(BLOSC_static_LIBRARY "blosc"
    PATHS
    ${BLOSC_ROOT} $ENV{BLOSC_ROOT}
    /usr/
    /usr/local/
    PATH_SUFFIXES "lib" "lib/x64" DOC
    "Path to dynamic library")

if (BLOSC_LINK_DYNAMIC)
    set(BLOSC_LIBRARIES ${BLOSC_dynamic_LIBRARY})
else ()
    set(BLOSC_LIBRARIES ${BLOSC_static_LIBRARY})
endif ()

find_package_handle_standard_args(BLOSC FOUND_VAR BLOSC_FOUND REQUIRED_VARS BLOSC_INCLUDE_DIR BLOSC_LIBRARIES)

mark_as_advanced(BLOSC_LIBRARIES)
