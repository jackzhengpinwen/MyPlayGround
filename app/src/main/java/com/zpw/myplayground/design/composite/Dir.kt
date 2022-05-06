package com.zpw.myplayground.design.composite

abstract class Dir(var name: String) {
    protected val dirs = ArrayList<Dir>()

    abstract fun addDir(dir: Dir)

    abstract fun rmDir(dir: Dir)

    abstract fun clear()

    abstract fun print()

    abstract fun getFiles(): List<Dir>
}