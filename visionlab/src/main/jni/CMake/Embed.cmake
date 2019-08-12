
# Creates C resources file from files in given directory
function(create_resources files output_c output_h)
    # Begin writing output files
    file(WRITE ${output_c} "#include \"${output_h}\"\nnamespace resources {\n")
    file(WRITE ${output_h} "#pragma once\n#include <cstddef>\n#include <cstdint>\nnamespace resources {\n")

    # Iterate through input files
    foreach(bin ${files})
        message("Embedding resource ${bin} ...")
        # Get short filename
        string(REGEX MATCH "([^/]+)$" filename ${bin})
        # Replace filename spaces & extension separator for C compatibility
        string(REGEX REPLACE "[-\\.| ]" "_" filename ${filename})
        # Read hex data from file
        file(READ ${bin} filedata HEX)
        # Convert hex data for C compatibility
        string(REGEX REPLACE "([0-9a-f][0-9a-f])" "0x\\1," filedata ${filedata})
        # Append data to output file
        file(APPEND ${output_c} "const uint8_t ${filename}[] = {${filedata}};\nconst size_t ${filename}_size = sizeof(${filename});\n")
        file(APPEND ${output_h} "extern const uint8_t ${filename}[];\nextern const size_t ${filename}_size;\n")
    endforeach()

    # Close namespaces
    file(APPEND ${output_c} "}\n")
    file(APPEND ${output_h} "}\n")
endfunction()
