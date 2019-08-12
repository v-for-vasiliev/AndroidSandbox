# Input:
#  PTHREADPOOL_ROOT - path to root directory. Also it's accepted as environment variable
# Output:
#  PTHREADPOOL_FOUND
#  PTHREADPOOL_INCLUDE_DIR
#  PTHREADPOOL_LIBRARIES=PTHREADPOOL_LIBRARY

include(FindPackageHandleStandardArgs)

find_path(PTHREADPOOL_INCLUDE_DIR "pthreadpool.h" PATHS ${PTHREADPOOL_ROOT} $ENV{PTHREADPOOL_ROOT} PATH_SUFFIXES "include" DOC "Path to directory with pthreadpool.h")
find_library(PTHREADPOOL_LIBRARY "pthreadpool" PATHS ${PTHREADPOOL_ROOT} $ENV{PTHREADPOOL_ROOT} PATH_SUFFIXES "lib" "lib/${ANDROID_ABI}" DOC "Path to pthreadpool library")

find_package_handle_standard_args(PTHREADPOOL FOUND_VAR PTHREADPOOL_FOUND REQUIRED_VARS PTHREADPOOL_INCLUDE_DIR PTHREADPOOL_LIBRARY)
