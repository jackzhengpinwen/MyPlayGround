package com.zpw.myplayground.design.composite

class Folder(name: String): Dir(name) {
    override fun addDir(dir: Dir) {
        dirs.add(dir)
    }

    override fun rmDir(dir: Dir) {
        dirs.remove(dir)
    }

    override fun clear() {
        dirs.clear()
    }

    override fun print() {
        print("$name(")
        val iterator = dirs.iterator()
        while (iterator.hasNext()) {
            val dir = iterator.next()
            dir.print()
            if (iterator.hasNext()) {
                print(", ")
            }
        }
        print(")")
    }

    override fun getFiles(): List<Dir> = dirs
}