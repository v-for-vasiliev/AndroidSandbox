# defines:
#   TBB_INCLUDE_DIRS
#   TBB_LIBRARIES

if(NOT HAVE_TBB)
  find_path(TBB_INCLUDE_DIRS "tbb/tbb.h" PATHS ${TBB_INCLUDE_DIR} DOC "The path to TBB headers")
  
  if(TBB_INCLUDE_DIRS)
	if(WIN32)
      if(CMAKE_COMPILER_IS_GNUCXX)
        set(TBB_LIB_DIR "${TBB_INCLUDE_DIRS}/../lib" CACHE PATH "Full path of TBB library directory")
        set(TBB_LIBRARIES ${TBB_LIBRARIES} debug tbb_debug optimized tbb)
      else()
		get_filename_component(_TBB_LIB_PATH "${TBB_INCLUDE_DIRS}/../lib" ABSOLUTE)
		get_filename_component(_TBB_BIN_PATH "${TBB_INCLUDE_DIRS}/../bin" ABSOLUTE)
		
		if(CMAKE_SIZEOF_VOID_P EQUAL 8)
			set(_TBB_ARCH "intel64")
		else()
			set(_TBB_ARCH "ia32")
		endif()

        if(MSVC80)
          set(_TBB_COMPILER "vc8")
        elseif(MSVC90)
          set(_TBB_COMPILER "vc9")
        elseif(MSVC10)
          set(_TBB_COMPILER "vc10")
        elseif(MSVC11)
          set(_TBB_COMPILER "vc11")
        elseif(MSVC12)
          set(_TBB_COMPILER "vc12")
        elseif(MSVC14)
          set(_TBB_COMPILER "vc14")
        endif()

		set(_TBB_LIB_PATH "${_TBB_LIB_PATH}/${_TBB_ARCH}/${_TBB_COMPILER}")
		set(_TBB_BIN_PATH "${_TBB_BIN_PATH}/${_TBB_ARCH}/${_TBB_COMPILER}")

		set(TBB_LIB_DIR "${_TBB_LIB_PATH}" CACHE PATH "Full path of TBB library directory")
		set(TBB_BIN_DIR "${_TBB_BIN_PATH}" CACHE PATH "Full path of TBB binary directory")
      endif()
	  set(TBB_LIBRARIES ${TBB_LIBRARIES} debug tbb_debug optimized tbb)
		get_filename_component(_TBB_LIB_PATH "${TBB_INCLUDE_DIRS}/../lib" ABSOLUTE)
		set(TBB_LIBRARIES ${TBB_LIBRARIES} tbb)
		set(TBB_LIB_DIR "${_TBB_LIB_PATH}" CACHE PATH "Full path of TBB library directory")
	else()
		get_filename_component(_TBB_LIB_PATH "${TBB_INCLUDE_DIRS}/../lib" ABSOLUTE)
		
		if(APPLE)
			set(TBB_LIBRARIES ${TBB_LIBRARIES} tbb)
		else()
			
			# Linux, Android
			if(CMAKE_SIZEOF_VOID_P EQUAL 8)
				set(_TBB_LIB_PATH "${_TBB_LIB_PATH}/intel64")
			else()
				set(_TBB_LIB_PATH "${_TBB_LIB_PATH}/ia32")
			endif()
			
			execute_process(COMMAND ${CMAKE_C_COMPILER} -dumpversion
							OUTPUT_VARIABLE _GCC_VERSION)

			if (_GCC_VERSION VERSION_GREATER 4.4 OR _GCC_VERSION VERSION_EQUAL 4.4)
				set(_TBB_LIB_PATH "${_TBB_LIB_PATH}/gcc4.4")
			else()
				set(_TBB_LIB_PATH "${_TBB_LIB_PATH}/gcc4.1")
			endif()

			if(ANDROID)
				set(TBB_LIBRARIES ${TBB_LIBRARIES} debug tbb_debug optimized tbb)
				add_definitions(-DTBB_USE_GCC_BUILTINS)
			elseif(UNIX)
				set(TBB_LIBRARIES ${TBB_LIBRARIES} debug tbb_debug optimized tbb)
			endif()
		endif()

		set(TBB_LIB_DIR "${_TBB_LIB_PATH}" CACHE PATH "Full path of TBB library directory")
    endif()


	link_directories("${TBB_LIB_DIR}")
	
    set(HAVE_TBB 1)

  endif(TBB_INCLUDE_DIRS)
endif(NOT HAVE_TBB)
