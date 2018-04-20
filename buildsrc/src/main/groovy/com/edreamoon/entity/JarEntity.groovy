package com.edreamoon.entity

class JarEntity extends BaseEntity {
    String jarZipDir
    boolean hasChanged
    boolean saveCache
    boolean removeCache
    boolean useCache

    JarEntity(File input, File output, String zipDir) {
        inputFile = input
        outputFile = output
        jarZipDir = zipDir
        hasChanged = false
        saveCache = false
        removeCache = false
        useCache = false
    }
}