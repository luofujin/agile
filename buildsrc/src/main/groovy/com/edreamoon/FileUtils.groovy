package com.edreamoon


class FileUtils {
    static void processManifest(def filePath, def set) {
        println("@processManifest path: $filePath")
        def file = new XmlSlurper().parse(filePath)
        println("333 ${file.application}")
        println("444 ${file.application.activity}")
        println("555 ${file.application.activity.'@android:name'}")
        file.application.activity.each { it ->
            def name = it."@android:name"
            println("@processManifest activity: ${name}")
            set.add(name)
        }
    }
}