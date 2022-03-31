package com.zpw.myplayground.model.generation

data class FileToBeGenerated(
    val name: String,
    val constantList: List<ConstantToBeGenerated>
)
