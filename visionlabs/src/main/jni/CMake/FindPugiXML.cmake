# Find the pugixml XML parsing library.
# Sets the usual variables expected for find_package scripts:
# PUGIXML_INCLUDE_DIR - header location
# PUGIXML_LIBRARIES - library to link against
# PUGIXML_FOUND - true if pugixml was found
# PUGIXML_DEFINITIONS - compiler switches required for using pugixml.
# PUGIXML_SOURCE_DIR - source location

set(PUGIXML_BUNDLED_HOME "${CMAKE_SOURCE_DIR}/../extern/pugixml")
set(PUGIXML_HOME "${PUGIXML_BUNDLED_HOME}" CACHE PATH "Path to PugiXML root")
 
find_path(PUGIXML_INCLUDE_DIR
		   NAMES pugixml.hpp pugiconfig.hpp
		   HINTS ${PUGIXML_BUNDLED_HOME}
		   PATHS ${PUGIXML_HOME}
		   PATH_SUFFIXES include src)
 
find_library(PUGIXML_LIBRARY
			 NAMES pugixml
			 HINTS ${PUGIXML_BUNDLED_HOME}
			 PATHS ${PUGIXML_HOME}
			 PATH_SUFFIXES lib)
 
find_library(PUGIXML_LIBRARYD
			 NAMES pugixmld
			 HINTS ${PUGIXML_BUNDLED_HOME}
			 PATHS ${PUGIXML_HOME}
			 PATH_SUFFIXES lib)
			 
find_path(PUGIXML_SOURCE_DIR
		   NAMES pugixml.cpp
		   HINTS ${PUGIXML_BUNDLED_HOME}
		   PATHS ${PUGIXML_HOME}
		   PATH_SUFFIXES include src)

# Handle pugixml header-only mode
if((${PUGIXML_LIBRARY} STREQUAL "PUGIXML_LIBRARY-NOTFOUND") AND
   (${PUGIXML_LIBRARYD} STREQUAL "PUGIXML_LIBRARYD-NOTFOUND"))
	set(PUGIXML_LIBRARY)
	set(PUGIXML_LIBRARYD)
	set(PUGIXML_DEFINITIONS "${PUGIXML_DEFINITIONS} -DPUGIXML_HEADER_ONLY")
 
	# Support the REQUIRED and QUIET arguments, and set PUGIXML_FOUND if found.
	include(FindPackageHandleStandardArgs)
	FIND_PACKAGE_HANDLE_STANDARD_ARGS(PUGIXML DEFAULT_MSG
									  PUGIXML_INCLUDE_DIR)
else()
	# Support the REQUIRED and QUIET arguments, and set PUGIXML_FOUND if found.
	include(FindPackageHandleStandardArgs)
	FIND_PACKAGE_HANDLE_STANDARD_ARGS(PUGIXML DEFAULT_MSG
									  PUGIXML_LIBRARY
									  PUGIXML_LIBRARYD
									  PUGIXML_INCLUDE_DIR)
endif()

if(PUGIXML_FOUND)
	if(PUGIXML_LIBRARY AND PUGIXML_LIBRARYD)
	  set(PUGIXML_LIBRARIES debug ${PUGIXML_LIBRARYD} optimized ${PUGIXML_LIBRARY})
	elseif(PUGIXML_LIBRARY)
	  set(PUGIXML_LIBRARIES ${PUGIXML_LIBRARY})
	elseif(PUGIXML_LIBRARYD)
	  set(PUGIXML_LIBRARIES ${PUGIXML_LIBRARYD})
	else()
	  set(PUGIXML_LIBRARIES)
	endif()
	set(PUGIXML_LIBRARY)
	set(PUGIXML_LIBRARYD)
	message(STATUS "PugiXML include = ${PUGIXML_INCLUDE_DIR}")
	if(PUGIXML_LIBRARIES)
	  message(STATUS "PugiXML libraries = ${PUGIXML_LIBRARIES}")
	else()
	  message(STATUS "PugiXML is used in header-only configuration")
	endif()
else()
	message(STATUS "No PugiXML found")
endif()
 
# Don't show in GUI
mark_as_advanced(
  PUGIXML_BUNDLED_HOME
  PUGIXML_INCLUDE_DIR
  PUGIXML_SOURCE_DIR
  PUGIXML_LIB_NAME_SUFFIX
  PUGIXML_LIBRARY
  PUGIXML_LIBRARYD
  PUGIXML_LIBRARIES)
