cmake_minimum_required(VERSION 3.22.1)
project("native-math")

add_library(${CMAKE_PROJECT_NAME} SHARED
    native-math.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    m
)
