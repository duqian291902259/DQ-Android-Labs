# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib
        SHARED
        native-lib.cpp
        dgif_lib.c
        gifalloc.c
        #[[egif_lib.c
        gif_err.c
        gif_font.c
        openbsd-reallocarray.c]]
        )
find_library(
        jnigraphics-lib
        jnigraphics)

find_library( # Sets the name of the path variable.
        log-lib
        log)
target_link_libraries(
        native-lib
        ${log-lib}
        ${jnigraphics-lib}
)