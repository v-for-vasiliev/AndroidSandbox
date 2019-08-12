# Input:
#  VLDNN_ROOT - path to root directory. Also it's accepted as environment variable
# Output:
#  VLDNN_FOUND
#  VLDNN_INCLUDE_DIR
#  VLDNN_LIBRARIES=VLDNN_LIBRARY

include(FindPackageHandleStandardArgs)

find_path(VLDNN_INCLUDE_DIR "vldnn.h" PATHS ${VLDNN_ROOT} $ENV{VLDNN_ROOT} PATH_SUFFIXES "include" "include/vldnn" DOC "Path to directory with vldnn header")
find_library(VLDNN_LIBRARY "vldnn" PATHS ${VLDNN_ROOT} $ENV{VLDNN_ROOT} PATH_SUFFIXES "lib" "lib/${ANDROID_ABI}" DOC "Path to vldnn library")

set(VLDNN_LIBRARIES ${VLDNN_LIBRARY})

find_package_handle_standard_args(VLDNN FOUND_VAR VLDNN_FOUND REQUIRED_VARS VLDNN_INCLUDE_DIR VLDNN_LIBRARIES)
