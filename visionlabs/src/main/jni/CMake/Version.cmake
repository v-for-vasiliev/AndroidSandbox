
# parse_version Macro
# This macro converts a version string into its three integer components.
#
# Usage:
#     parse_version(MAJOR MINOR PATCH ${VERSION})
#
# Parameters:
#     MAJOR      The variable to store the major integer component in.
#     MINOR      The variable to store the minor integer component in.
#     PATCH      The variable to store the patch integer component in.
#     VERSION    The version string to convert ("v.#.#.#" format).
macro(parse_version MAJOR MINOR PATCH VERSION)
    string(REGEX REPLACE "v.([0-9]+)\\..*" "\\1" ${MAJOR} ${VERSION})
    string(REGEX REPLACE "v.[0-9]+\\.([0-9]+).*" "\\1" ${MINOR} ${VERSION})
    string(REGEX REPLACE "v.[0-9]+\\.[0-9]+\\.([0-9]+).*" "\\1" ${PATCH} ${VERSION})
endmacro()
