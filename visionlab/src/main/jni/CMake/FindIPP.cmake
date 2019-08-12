if(NOT HAVE_IPP)
  find_path(IPP_INCLUDE_DIRS "ipp.h" PATHS ${IPP_INCLUDE_DIR} DOC "The path to IPP headers")

  if(IPP_INCLUDE_DIRS)

		get_filename_component(_IPP_LIB_PATH "${IPP_INCLUDE_DIRS}/../lib" ABSOLUTE)
		
		if(CMAKE_SIZEOF_VOID_P EQUAL 8)
			set(_IPP_LIB_PATH "${_IPP_LIB_PATH}/intel64")
			set(_IPP_COMPILER_LIB_PATH ${IPP_INCLUDE_DIRS}/../../compiler/lib/intel64)
			if(WIN32)
				set(_IPP_COMPILER_REDIST_PATH ${IPP_INCLUDE_DIRS}/../../redist/intel64/compiler)
			endif()
		else()
			set(_IPP_LIB_PATH "${_IPP_LIB_PATH}/ia32")
			set(_IPP_COMPILER_LIB_PATH ${IPP_INCLUDE_DIRS}/../../compiler/lib/ia32)
			if(WIN32)
				set(_IPP_COMPILER_REDIST_PATH ${IPP_INCLUDE_DIRS}/../../redist/ia32/compiler)
			endif()
		endif()

		# On Linux take libraries for redistribution from the same location that is used for linking
		if(UNIX)
			set(_IPP_COMPILER_REDIST_PATH ${_IPP_COMPILER_LIB_PATH})
		endif()
		
		get_filename_component(_IPP_LIB_PATH "${_IPP_LIB_PATH}" ABSOLUTE)
		get_filename_component(_IPP_COMPILER_LIB_PATH "${_IPP_COMPILER_LIB_PATH}" ABSOLUTE)
		get_filename_component(_IPP_COMPILER_REDIST_PATH "${_IPP_COMPILER_REDIST_PATH}" ABSOLUTE)
		
		if(WIN32)
			set(IPP_LIBRARIES ${IPP_LIBRARIES} ippi ippcore   ipps)
		else()
			set(IPP_LIBRARIES ${IPP_LIBRARIES}
			libippi.a libippcore.a libipps.a)
		endif()


		set(IPP_LIB_DIR "${_IPP_LIB_PATH}"
			CACHE PATH "Full path of IPP library directory")	
		set(IPP_COMPILER_LIB_DIR "${_IPP_COMPILER_LIB_PATH}"
			CACHE PATH "Full path of IPP compiler library directory")
		set(IPP_COMPILER_REDIST_PATH "${_IPP_COMPILER_REDIST_PATH}"
			CACHE PATH "Full path of IPP compiler redistributable library directory")
						
		#message("IPP_LIB_DIR ${IPP_LIB_DIR}")
		#message("IPP_COMPILER_LIB_DIR ${IPP_COMPILER_LIB_DIR}")
		#message("IPP_COMPILER_REDIST_PATH ${IPP_COMPILER_REDIST_PATH}")

	link_directories(${IPP_LIB_DIR} ${IPP_COMPILER_LIB_DIR})

    set(HAVE_IPP 1)

  endif(IPP_INCLUDE_DIRS)
endif(NOT HAVE_IPP)
