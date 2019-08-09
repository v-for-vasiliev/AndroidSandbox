function(filter INPUT_LIST REGEXP OUTPUT_LIST)

	set(RESULT)
	FOREACH(item ${INPUT_LIST})
	  STRING(REGEX MATCH ${REGEXP} item ${item})
	  IF(item)
	    LIST(APPEND RESULT ${item})
	  ENDIF()
	ENDFOREACH()
	
	set(${OUTPUT_LIST} ${RESULT} PARENT_SCOPE)
	
endfunction()