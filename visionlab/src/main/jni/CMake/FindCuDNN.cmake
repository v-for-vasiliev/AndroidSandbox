# Input:
#  CuDNN_ROOT - path to root directory. Also it's accepted as environment variable
# Output:
#  CuDNN_FOUND
#  CuDNN_VERSION=CuDNN_VERSION_MAJOR.CuDNN_VERSION_MINOR
#  CuDNN_INCLUDE_DIR
#  CuDNN_LIBRARIES=CuDNN_LIBRARY

include(FindPackageHandleStandardArgs)

find_path(CuDNN_INCLUDE_DIR "cudnn.h" PATHS ${CuDNN_ROOT} $ENV{CuDNN_ROOT} PATH_SUFFIXES "include" DOC "Path to directory with CuDNN header")
find_library(CuDNN_LIBRARY "cudnn" PATHS ${CuDNN_ROOT} $ENV{CuDNN_ROOT} PATH_SUFFIXES "lib" "lib/x64" DOC "Path to CuDNN library")

if (CuDNN_INCLUDE_DIR)
  file(STRINGS ${CuDNN_INCLUDE_DIR}/cudnn.h _CuDNN_VERSION_DATA REGEX "\#define CUDNN_(MAJOR|MINOR)")
  list(GET _CuDNN_VERSION_DATA 0 CuDNN_VERSION_MAJOR)
  list(GET _CuDNN_VERSION_DATA 1 CuDNN_VERSION_MINOR)
  string(REGEX REPLACE ".*MAJOR[\\t ]*([0-9]+)" "\\1" CuDNN_VERSION_MAJOR ${CuDNN_VERSION_MAJOR})
  string(REGEX REPLACE ".*MINOR[\\t ]*([0-9]+)" "\\1" CuDNN_VERSION_MINOR ${CuDNN_VERSION_MINOR})
endif ()

set(CuDNN_LIBRARIES ${CuDNN_LIBRARY})
set(CuDNN_VERSION ${CuDNN_VERSION_MAJOR}.${CuDNN_VERSION_MINOR})

find_package_handle_standard_args(CuDNN FOUND_VAR CuDNN_FOUND REQUIRED_VARS CuDNN_INCLUDE_DIR CuDNN_LIBRARIES VERSION_VAR CuDNN_VERSION)

mark_as_advanced(CuDNN_LIBRARIES)
mark_as_advanced(CuDNN_VERSION_MAJOR CuDNN_VERSION_MINOR)
