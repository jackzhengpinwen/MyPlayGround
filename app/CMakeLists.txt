cmake_minimum_required(VERSION 3.4.1)

add_library(
        entrypoint
        SHARED
        src/main/cpp/entrypoint.cpp
        )

include_directories(
        src/main/cpp
)

target_link_libraries(
        entrypoint
        log
)
