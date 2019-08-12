# Input:
#  NNPACK_ROOT - path to root directory. Also it's accepted as environment variable
# Output:
#  NNPACK_FOUND
#  NNPACK_INCLUDE_DIR
#  NNPACK_LIBRARIES=NNPACK_LIBRARY NNPACK_UKERNELS_LIBRARY [NNPACK_CPUFEATURES_LIBRARY for armeabi-v7a]

include(FindPackageHandleStandardArgs)

find_path(NNPACK_INCLUDE_DIR "nnpack.h" PATHS ${NNPACK_ROOT} $ENV{NNPACK_ROOT} PATH_SUFFIXES "include" DOC "Path to directory with nnpack header")
find_library(NNPACK_LIBRARY "nnpack" PATHS ${NNPACK_ROOT} $ENV{NNPACK_ROOT} PATH_SUFFIXES "lib" "lib/${ANDROID_ABI}" DOC "Path to nnpack library")

if (ANDROID)
    find_library(NNPACK_UKERNELS_LIBRARY "nnpack_ukernels" PATHS ${NNPACK_ROOT} $ENV{NNPACK_ROOT} PATH_SUFFIXES "lib" "lib/${ANDROID_ABI}" DOC "Path to nnpack_ukernels library")
endif ()

set(NNPACK_LIBRARIES ${NNPACK_LIBRARY})

if (NNPACK_UKERNELS_LIBRARY)
    list(APPEND NNPACK_LIBRARIES ${NNPACK_UKERNELS_LIBRARY})
endif ()

if (ANDROID)
    if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
        find_library(NNPACK_CPUFEATURES_LIBRARY "cpufeatures" PATHS ${NNPACK_ROOT} $ENV{NNPACK_ROOT} PATH_SUFFIXES "lib" "lib/${ANDROID_ABI}" DOC "Path to cpufeatures library")
        list(APPEND NNPACK_LIBRARIES ${NNPACK_CPUFEATURES_LIBRARY})
    endif ()
endif ()

find_package_handle_standard_args(NNPACK FOUND_VAR NNPACK_FOUND REQUIRED_VARS NNPACK_INCLUDE_DIR NNPACK_LIBRARIES)

mark_as_advanced(NNPACK_LIBRARIES NNPACK_UKERNELS_LIBRARY NNPACK_CPUFEATURES_LIBRARY)
