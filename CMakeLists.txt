message(STATUS "Building with CMake V${CMAKE_VERSION}")
project(navit C)


# Workaround for CMake issue 8345 / 9220, see http://trac.navit-project.org/ticket/1041
if(DEFINED CMAKE_CXX_COMPILER AND CMAKE_CXX_COMPILER MATCHES "^$")
  set(CMAKE_CXX_COMPILER CMAKE_CXX_COMPILER-NOTFOUND)
endif(DEFINED CMAKE_CXX_COMPILER AND CMAKE_CXX_COMPILER MATCHES "^$")
if (NOT DISABLE_CXX)
  enable_language(CXX OPTIONAL)
endif(NOT DISABLE_CXX)

cmake_minimum_required(VERSION 2.6 FATAL_ERROR)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/")
include("${PROJECT_SOURCE_DIR}/cmake/navit_macros.cmake")

set(CMAKE_BUILD_TYPE "Release")

### Detect environment

add_plugin(support/glib "native Glib found" FALSE)
add_plugin(support/gettext_intl "native libintl found" FALSE)
add_module(graphics/android "FreeType library not found" FALSE)

if(WIN32)
    add_feature(USE_NSIS "makensis not found" FALSE)
    FIND_PROGRAM(NSIS_EXECUTABLE NAMES makensis makensis.exe)
    if(NSIS_EXECUTABLE)
        set_with_reason(USE_NSIS "makensis found" TRUE)
    endif(NSIS_EXECUTABLE)

    set(CPACK_GENERATOR ZIP)
    if(USE_NSIS)
        list(APPEND CPACK_GENERATOR NSIS)
    endif(USE_NSIS)
    set(CPACK_PACKAGE_INSTALL_DIRECTORY Navit)
endif(WIN32)
INCLUDE (CPack)

INCLUDE (CheckIncludeFiles)
INCLUDE (CheckLibraryExists)
INCLUDE (CheckFunctionExists)
INCLUDE (CheckSymbolExists)

find_package(Glib)
find_package(Gmodule)
find_package(Gettext)
find_package(Threads)



#Simple checks
CHECK_INCLUDE_FILES(wordexp.h HAVE_WORDEXP)
CHECK_INCLUDE_FILES(endian.h HAVE_ENDIAN_H)
CHECK_INCLUDE_FILES(stdint.h HAVE_STDINT_H)
CHECK_INCLUDE_FILES(byteswap.h HAVE_BYTESWAP_H)
CHECK_LIBRARY_EXISTS(gypsy gypsy_control_get_default "" GYPSY_FOUND)
CHECK_INCLUDE_FILES(libspeechd.h HAVE_LIBSPEECHD)
CHECK_INCLUDE_FILES(sys/socket.h HAVE_SOCKET)
CHECK_INCLUDE_FILES(sys/shm.h HAVE_SHMEM)
CHECK_FUNCTION_EXISTS(snprintf   HAVE_SNPRINTF)
if (NOT HAVE_SNPRINTF)
   CHECK_FUNCTION_EXISTS(_snprintf  HAVE_DECL__SNPRINTF)
   if (HAVE_DECL__SNPRINTF)
      set(HAVE_SNPRINTF 1)
   endif(HAVE_DECL__SNPRINTF)
endif(NOT HAVE_SNPRINTF)
CHECK_INCLUDE_FILES(locale.h HAVE_LC_MESSAGES)
CHECK_INCLUDE_FILES(libintl.h HAVE_LIBINTL)
CHECK_INCLUDE_FILES(sys/time.h HAVE_SYS_TIME_H)
CHECK_INCLUDE_FILES(getopt.h HAVE_GETOPT_H)
CHECK_INCLUDE_FILES(string.h HAVE_STRING_H)
CHECK_FUNCTION_EXISTS(gettimeofday HAVE_GETTIMEOFDAY)
CHECK_SYMBOL_EXISTS(system stdlib.h HAVE_SYSTEM)
CHECK_SYMBOL_EXISTS(CreateProcess windows.h HAVE_CREATEPROCESS)
CHECK_SYMBOL_EXISTS(stpcpy string.h HAVE_STPCPY)
CHECK_FUNCTION_EXISTS(sbrk HAVE_SBRK)
CHECK_FUNCTION_EXISTS(getdelim HAVE_GETDELIM)
CHECK_FUNCTION_EXISTS(getline HAVE_GETLINE)
CHECK_FUNCTION_EXISTS(fsync HAVE_FSYNC)

### Configure build
#Required libraries, supplied with navit
if (NOT HAVE_LIBINTL)
   set_with_reason(support/gettext_intl "native libintl missing" TRUE)
   set(USE_LIBGNUINTL TRUE)
else()
   CHECK_LIBRARY_EXISTS(intl libintl_fprintf "" NEEDS_LIBINTL)
   if (NEEDS_LIBINTL)
      list(APPEND NAVIT_LIBS intl)
      set(INTL_LIBS intl)
   endif(NEEDS_LIBINTL)
endif(NOT HAVE_LIBINTL)

if (CMAKE_USE_PTHREADS_INIT)
   if (NOT ANDROID)
      list(APPEND NAVIT_LIBS pthread)
   endif(NOT ANDROID)
endif(CMAKE_USE_PTHREADS_INIT)
if (Glib_FOUND)
   set(HAVE_GLIB 1)
   include_directories(${Glib_INCLUDE_DIRS})
   list(APPEND NAVIT_LIBS ${Glib_LIBRARIES})
else(Glib_FOUND)
   set_with_reason(support/glib "Glib not found" TRUE ${INTL_LIBS})
endif(Glib_FOUND)
if (Gmodule_FOUND)
   set(HAVE_GMODULE 1)
   include_directories(${Gmodule_INCLUDE_DIRS})
   list(APPEND NAVIT_LIBS ${Gmodule_LIBRARIES})
endif(Gmodule_FOUND)
if(ZLIB_FOUND)
   set(HAVE_ZLIB 1)
   include_directories(${ZLIB_INCLUDE_DIRS})
   list(APPEND NAVIT_LIBS ${ZLIB_LIBRARIES})
else(ZLIB_FOUND)
   message(STATUS "using internal zlib")
   set_with_reason(support/zlib "native zlib missing" TRUE)
endif(ZLIB_FOUND)
if(OPENSSL_CRYPTO_LIBRARIES)
   set(HAVE_LIBCRYPTO 1)
   include_directories(${OPENSSL_INCLUDE_DIR})
   list(APPEND NAVIT_LIBS ${OPENSSL_CRYPTO_LIBRARIES})
endif()
if(PNG_FOUND)
   set(HAVE_PNG 1)
   include_directories(${PNG_INCLUDE_DIR})
   list(APPEND NAVIT_LIBS ${PNG_LIBRARIES})
else(PNG_FOUND)
   message(STATUS "using internal libpng")
   set_with_reason(support/libpng "native libpng missing" TRUE)
endif(PNG_FOUND)
if (NOT HAVE_WORDEXP)
   message(STATUS "wordexp.h not found. use internal wordexp")
   set_with_reason(support/wordexp "native wordexp missing" TRUE)
endif()
if (NOT Glib_FOUND)
   set_with_reason(support/ezxml "Glib not found" TRUE)
endif()

if(FREETYPE_FOUND)
   pkg_check_modules(FRIBIDI fribidi)
   pkg_check_modules(FRIBIDI2 fribidi>=0.19.0)
   include_directories(${FREETYPE_INCLUDE_DIRS})
   set_with_reason(font/freetype "freetype found" TRUE "${FREETYPE_LIBRARY};${FONTCONFIG_LDFLAGS};${FRIBIDI_LIBRARIES}")
else(FREETYPE_FOUND)
   MESSAGE("No freetype library found, graphics modules may not be available")
   set_with_reason(graphics/android "FreeType library not found" FALSE)
   set_with_reason(graphics/gd "FreeType library not found" FALSE)
   set_with_reason(graphics/gtk_drawing_area "FreeType library not found" FALSE)
   set_with_reason(graphics/opengl "FreeType library not found" FALSE)
   set_with_reason(graphics/sdl "FreeType library not found" FALSE)
   set_with_reason(graphics/egl "FreeType library not found" FALSE)
endif(FREETYPE_FOUND)

if(FONTCONFIG_FOUND)
   set(HAVE_FONTCONFIG 1)
endif(FONTCONFIG_FOUND)

if (GETTEXT_FOUND)
    set_with_reason(USE_NATIVE_LANGUAGE_SUPPORT "Gettext found" TRUE)
endif(GETTEXT_FOUND)


#Independent modules
add_module(graphics/null "Default" TRUE)
add_module(osd/core "Default" TRUE)
add_module(vehicle/demo "Default" TRUE)
add_module(vehicle/file "Default" TRUE)
add_module(vehicle/null "Default" FALSE)
add_module(map/binfile "Default" TRUE)
add_module(map/filter "Default" TRUE)
add_module(map/textfile "Default" TRUE)
add_module(map/csv "Default" TRUE)

#Modules without test yet
add_module(speech/android "Default" FALSE)
add_module(vehicle/android "Default" FALSE)

# other features
add_feature(USE_PLUGINS "default" TRUE)
add_feature(USE_ROUTING "default" TRUE)



if(ANDROID)
   find_program(ANDROID_LOCATION NAMES android android.bat)
   if (NOT ANDROID_LOCATION)
      message_error( "Could not find 'Android SDK and AVD Manager'. Please install the android sdk and add the <android-sdk>/tools directory to the search path.")
   endif()
   set(XSLTS "android;${XSLTS}" CACHE STRING "define a semicolon seperated list of XSLTs to process")
   list(APPEND NAVIT_LIBS log)
   set(HAVE_API_ANDROID 1)
   set_with_reason(graphics/android "Android detected" TRUE)
   set_with_reason(speech/android "Android detected" TRUE)
   set_with_reason(vehicle/android "Android detected" TRUE)
   set(SHARED_LIBNAVIT TRUE)

   add_feature(XPM2PNG "Android detected" TRUE)
   set(NAVIT_COMPILE_FLAGS "${NAVIT_COMPILE_FLAGS} -fPIC")

endif(ANDROID)


CHECK_INCLUDE_FILES (malloc.h HAVE_MALLOC_H)
CHECK_INCLUDE_FILES ("sys/param.h;sys/mount.h" HAVE_SYS_MOUNT_H)
CHECK_INCLUDE_FILES (unistd.h HAVE_UNISTD_H)
CHECK_FUNCTION_EXISTS (getcwd HAVE_GETCWD)
CHECK_FUNCTION_EXISTS (memalign HAVE_MEMALIGN)
CHECK_FUNCTION_EXISTS (valloc HAVE_VALLOC)
CHECK_FUNCTION_EXISTS (posix_memalign HAVE_POSIX_MEMALIGN)
CHECK_FUNCTION_EXISTS (popen HAVE_POPEN)
CHECK_FUNCTION_EXISTS (_atoi64 HAVE__ATOI64)

CONFIGURE_FILE(${CMAKE_CURRENT_SOURCE_DIR}/config.h.cmake ${CMAKE_CURRENT_BINARY_DIR}/config.h)

if(CMAKE_COMPILER_IS_GNUCC OR CCMAKE_COMPILER_IS_GNUCXX)
   set(COMMON_COMPILER_FLAGS "-Wall -Wundef -Wcast-align -Wpointer-arith -Wno-unused-parameter -Wno-sign-compare")
   set(CMAKE_C_FLAGS   "${CMAKE_C_FLAGS} ${COMMON_COMPILER_FLAGS}")
   set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} ${COMMON_COMPILER_FLAGS}")
   # flags not available in old GCC versions, or not for both C and C++
   add_compiler_flag_if_available("-Wno-missing-field-initializers")
   add_compiler_flag_if_available("-Wextra")
   add_compiler_flag_if_available("-Wmissing-prototypes")
   add_compiler_flag_if_available("-Wstrict-prototypes ")
endif()

if (EXTRA_DEFINES)
	add_definitions("${EXTRA_DEFINES}")
endif(EXTRA_DEFINES)

add_subdirectory (navit)

















