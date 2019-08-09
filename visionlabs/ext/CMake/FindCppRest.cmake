# FindCasablanca package
#
# Set CPPREST_ROOT_DIR to guide the package to the casablanca installation.
# The script will set CPPREST_INCLUDE_DIR pointing to the location of header files.
# and CPPREST_LIBRARIES will be a list that can be passed to taget_link_libraries.
# The script checks for cpprest/http_client.h and cpprest/json.h header files only.
# It searches for casablanca.lib casablanca120.lib libcasablanca.so and libcasablanca.a,
# But will only add one of them to the link list.

if(UNIX)
	find_package(PkgConfig)
	pkg_check_modules(_CPPREST QUIET casablanca)
endif()

set(CPPREST_ROOT_DIR CACHE PATH "Path to CppRest root dir")
set(_CPPREST_ROOT_HINTS
		${CPPREST_ROOT_DIR})

if (WIN32)

	file(TO_CMAKE_PATH "$ENV{PROGRAMFILES}" _programfiles)
	set(_CPPREST_ROOT_PATHS
		"${CPPREST_ROOT_DIR}"
	)
	unset(_programfiles)
	set(_CPPREST_ROOT_HINTS_AND_PATHS
		HINTS ${_CPPREST_ROOT_HINTS}
		PATHS ${_CPPREST_ROOT_PATHS}

	)
else()
	set(_CPPREST_ROOT_HINTS
		${CPPREST_ROOT_DIR}
		${CPPREST_ROOT_DIR}
	)
endif(WIN32)

find_path(CPPREST_INCLUDE_DIR
	NAMES
	cpprest/http_client.h
	cpprest/json.h
	HINTS
	${_CPPREST_ROOT_HINTS_AND_PATHS}
	PATH_SUFFIXES
	include
	include/cpprest
	include/pplx
	include/compat
	include/casablanca
	include/casablanca/cpprest
	include/casablanca/pplx
	include/casablanca/compat
)

#message("CPPREST_INCLUDE_DIR ${CPPREST_INCLUDE_DIR}")

set(LIB_CPPREST)
if(WIN32 AND NOT CYGWIN)
	if(MSVC)
		SET(CMAKE_FIND_LIBRARY_PREFIXES "")
		# check msvc version and add to suffix list
		set(MSVC_SUFFIX)
		message("MSVC_SUFFIX ${MSVC14}")
		if(MSVC14)
			set(MSVC_SUFFIX "140_2_6.lib")
		elseif(MSVC12)
			set(MSVC_SUFFIX "120_2_4.lib")
		elseif(MSVC11)
			set(MSVC_SUFFIX "110.lib")
		elseif(MSVC10)
			set(MSVC_SUFFIX "100.lib")
		elseif(MSVC90)
			set(MSVC_SUFFIX "90.lib")
		elseif(MSVC80)
			set(MSVC_SUFFIX "80.lib")
		elseif(MSVC71)
			set(MSVC_SUFFIX "71.lib")
		elseif(MSVC11)
			set(MSVC_SUFFIX "70.lib")
		endif()
		if(MSVC14)
			set(MSVC_SUFFIXD "140d_2_6.lib")
		elseif(MSVC12)
			set(MSVC_SUFFIXD "120d_2_4.lib")
		elseif(MSVC11)
			set(MSVC_SUFFIXD "110d.lib")
		elseif(MSVC10)
			set(MSVC_SUFFIXD "100d.lib")
		elseif(MSVC90)
			set(MSVC_SUFFIXD "90d.lib")
		elseif(MSVC80)
			set(MSVC_SUFFIXD "80d.lib")
		elseif(MSVC71)
			set(MSVC_SUFFIXD "71d.lib")
		elseif(MSVC11)
			set(MSVC_SUFFIXD "70d.lib")
		endif()

		SET(CMAKE_FIND_LIBRARY_SUFFIXES ".lib" "${MSVC_SUFFIX}" "${MSVC_SUFFIXD}")

		if( CMAKE_SIZEOF_VOID_P EQUAL 8 )
			set(BITNESS "x86")
		else()
			set(BITNESS "x64")
		endif()
		find_library(LIB_CPPREST_RELEASE
			NAMES cpprest
			PATH ${_CPPREST_ROOT_HINTS_AND_PATHS}
			PATH_SUFFIXES "Binaries/x64/Release"
		)
		
		find_library(LIB_CPPREST_DEBUG
			NAMES cpprest
			PATH ${_CPPREST_ROOT_HINTS_AND_PATHS}
			PATH_SUFFIXES "Binaries/x64/Debug"
		)
		foreach(LIB ${LIB_CPPREST_DEBUG})
			list(APPEND LIB_CPPREST debug ${LIB})
		endforeach()

		foreach(LIB ${LIB_CPPREST_RELEASE})
			list(APPEND LIB_CPPREST optimized ${LIB})
		endforeach()
		

	endif(MSVC)
elseif(UNIX)
	SET(CMAKE_FIND_LIBRARY_PREFIXES "lib")
	SET(CMAKE_FIND_LIBRARY_SUFFIXES ".so" ".a")
	find_library(LIB_CPPREST
		NAMES
		cpprest
		HINTS
		${CPPREST_ROOT_DIR}
		${_CPPREST_ROOT_HINTS_AND_PATHS}
		PATH_SUFFIXES
		lib
	)
endif(WIN32 AND NOT CYGWIN)

mark_as_advanced(LIB_CPPREST)

#message("LIB_CPPREST ${LIB_CPPREST}")

if(WIN32 AND NOT CYGWIN)
	set(CPPREST_LIBRARIES "${LIB_CPPREST}" "crypt32.lib" "winhttp.lib"
				"bcrypt.lib")
	
elseif(UNIX)
	set(CPPREST_LIBRARIES ${LIB_CPPREST})
endif(WIN32 AND NOT CYGWIN)

if (CPPREST_INCLUDE_DIR)
	if(_CPPREST_VERSION)
		set(CPPREST_VERSION "${_CPPREST_VERSION}")
	elseif(CPPREST_INCLUDE_DIR AND EXISTS "${CPPREST_INCLUDE_DIR}/version.h")
		file(STRINGS "${CPPREST_INCLUDE_DIR}/freerdp/version.h" _casablanca_major_version
		REGEX "^#define[\t ]+CPPREST_VERSION_MAJOR[\t ]+[0-9]")

		string(REGEX REPLACE "^#define[\t ]+CPPREST_VERSION_MAJOR[\t ]+" "" 
			CPPREST_MAJOR_VERSION ${_casablanca_major_version})

		file(STRINGS "${CPPREST_INCLUDE_DIR}/version.h" _casablanca_minor_version
		REGEX "^#define[\t ]+CPPREST_VERSION_MINOR[\t ]+[0-9]")

		string(REGEX REPLACE "^#define[\t ]+CPPREST_VERSION_MINOR[\t ]+" "" 
			CPPREST_MINOR_VERSION ${_casablanca_minor_version})

		file(STRINGS "${CPPREST_INCLUDE_DIR}/version.h" _casablanca_revision_version
		REGEX "^#define[\t ]+CPPREST_VERSION_REVISION[\t ]+[0-9]")

		string(REGEX REPLACE "^#define[\t ]+CPPREST_VERSION_REVISION[\t ]+" "" 
			CPPREST_REVISION_VERSION ${_casablanca_revision_version})

		SET(CPPREST_VERSION "${CPPREST_MAJOR_VERSION}.${CPPREST_MINOR_VERSION}.${CPPREST_REVISION_VERSION}")
	endif(_CPPREST_VERSION)
endif(CPPREST_INCLUDE_DIR)

if(CPPREST_LIBRARIES AND CPPREST_INCLUDE_DIR)
	if(CPPREST_VERSION)
		find_package_handle_standard_args(CppRest
			REQUIRED_VARS
			 CPPREST_LIBRARIES
			 CPPREST_INCLUDE_DIR
			 VERSION_VAR
			  CPPREST_VERSION
			 FAIL_MESSAGE
			 "Could NOT find CPP REST, try to set the path to Casablanca root folder in the system variable CPPREST_ROOT_DIR"
		)
	else()
		find_package_handle_standard_args(CppRest
			REQUIRED_VARS
			 CPPREST_LIBRARIES
			 CPPREST_INCLUDE_DIR
			 FAIL_MESSAGE
			"Could NOT find CPP REST, try to set the path to Casablanca root folder in the system variable CPPREST_ROOT_DIR"
		)
	endif(CPPREST_VERSION)
else()
	find_package_handle_standard_args(CppRest "Could NOT find Casablanca, try to set the path to Casablanca root folder in the system variable CPPREST_ROOT_DIR"
		CPPREST_LIBRARIES
		CPPREST_INCLUDE_DIR
	)
endif()

mark_as_advanced(CPPREST_INCLUDE_DIR CPPREST_LIBRARIES)