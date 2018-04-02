package com.edreamoon

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlu implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("☆☆☆  RouterPlu apply start ☆☆☆")
        project.android.registerTransform(new RouterTransform(project))

        //读取Activity列表，来过滤注解activity
        project.android.applicationVariants.all { variant ->
            variant.outputs.each { output ->
                output.processManifest.outputs.upToDateWhen { false }
                output.processManifest.doLast {

                    ArrayList<File> manifestFileList = new ArrayList<>()
                    [output.processManifest.manifestOutputDirectory,
                     output.processManifest.instantRunManifestOutputDirectory
                    ].each { File directory ->
                        if (directory.exists()) {
                            println("@RouterPlu manifest dir path: ${directory.getAbsolutePath()}")
                            def file = new File(directory, "AndroidManifest.xml")
                            if (file.exists()) {
                                manifestFileList.add(file)
                            }
                        }
                    }

                    manifestFileList.each { File manifestOutFile ->
                        if (manifestOutFile.exists()) {
                            println("@RouterPlu manifest file Path: ${manifestOutFile.absolutePath}")
                            FileUtils.processManifest(manifestOutFile.absolutePath, AppInfo.activities)
                        }
                    }
                }
            }
        }

        println("☆☆☆  RouterPlu end start ☆☆☆")
    }
}