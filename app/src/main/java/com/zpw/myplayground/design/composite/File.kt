package com.zpw.myplayground.design.composite

class File(name: String): Dir(name) {
    override fun addDir(dir: Dir) {
        throw UnsupportedOperationException("File don't support this operation")
    }

    override fun rmDir(dir: Dir) {
        throw UnsupportedOperationException("File don't support this operation")
    }

    override fun clear() {
        throw UnsupportedOperationException("File don't support this operation")
    }

    override fun print() = print(name)

    override fun getFiles(): List<Dir> {
        throw UnsupportedOperationException("File don't support this operation")
    }

}