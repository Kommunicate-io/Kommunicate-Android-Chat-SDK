# CMakeLists.txt

cmake_minimum_required(VERSION 3.10.2)

project("rootcheck")  # Project name

add_library( # Define your libraries
        root_detection  # Library name
        SHARED          # Build a shared library
        root_detection.cpp  # Source files
        frida_detection.cpp
        memory_check.cpp
        anti_hooking.cpp
)

# Include libraries required for android native development
find_library(
        log-lib
        log
)

target_link_libraries(
        root_detection  # Link log library
        ${log-lib}
)
