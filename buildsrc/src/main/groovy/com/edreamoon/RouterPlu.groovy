package com.edreamoon

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlu implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("☆☆☆  RouterPlu apply start ☆☆☆")
        project.android.registerTransform(new RouterTransform(project))

        project.android.applicationVariants.all { variant ->
            variant.outputs.each { output ->
                output.processManifest.outputs.upToDateWhen { false }
                output.processManifest.doLast {
                    println("setting buildTypeName ${variant.buildType.name}")
                    ValueHolder.activities.clear()
//                        ValueHolder.sActivityMapping.clear()
                    ArrayList<File> manifestFileList = new ArrayList<>()

                    [output.processManifest.manifestOutputDirectory,
                     output.processManifest.instantRunManifestOutputDirectory
                    ].each { File directory ->
                        File mFile = new File(directory, "AndroidManifest.xml")
                        println("adding real manifest path ${mFile.getAbsolutePath()}")
                        manifestFileList.add(mFile)
                    }

                    manifestFileList.each { File manifestOutFile ->
                        if (manifestOutFile.exists()) {
                            println("adding manifestPath ${manifestOutFile.absolutePath}")
                            ClassModifier.processManifest(manifestOutFile.absolutePath, ValueHolder.activities)
                        }
                    }
                }
            }
        }

        println("☆☆☆  RouterPlu end ☆☆☆")
    }
}