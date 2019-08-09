function(getSymLinkChain START_FILE RETURN)

	#set(_result "${START_FILE} Hello")
	#set(${RETURN} ${_result} PARENT_SCOPE)
	
	#Make sure the initial path is absolute.
	get_filename_component(lib "${START_FILE}" ABSOLUTE)
	
	#Store initial path as first element in list.
	set(symlist "${lib}")
	
	while(UNIX AND IS_SYMLINK "${lib}")
	  #Grab path to directory containing the current symlink.
	  get_filename_component(sym_path "${lib}" DIRECTORY)
	
	  #Resolve one level of symlink, store resolved path back in lib.
	  execute_process(COMMAND readlink "${lib}"
	                  RESULT_VARIABLE errMsg
	                  OUTPUT_VARIABLE lib
	                  OUTPUT_STRIP_TRAILING_WHITESPACE)
	
	  #Check to make sure readlink executed correctly.
	  if(errMsg AND (NOT "${errMsg}" EQUAL "0"))
	    message(FATAL_ERROR "Error calling readlink on library.")
	  endif()
	
	  #Convert resolved path to an absolute path, if it isn't one already.
	  if(NOT IS_ABSOLUTE "${lib}")
	    set(lib "${sym_path}/${lib}")
	  endif()
	
	  #Append resolved path to symlink resolution list.
	  list(APPEND symlist "${lib}")
	endwhile()
	
	set(${RETURN} ${symlist} PARENT_SCOPE)

endfunction()