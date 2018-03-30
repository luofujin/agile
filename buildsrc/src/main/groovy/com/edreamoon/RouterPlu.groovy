package com.edreamoon

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlu implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("RouterPlu apply")
        project.android.registerTransform(new RouterTransform(project))
        project.android.applicationVariants.all { variant ->
            variant.outputs.each { output ->
                output.processManifest.doLast {
                    def manifestOutFile = output.processManifest.manifestOutputDirectory
                    def newFileContents = manifestOutFile.getText('UTF-8')
                    println("RouterPlu mainfest=$newFileContents")
//                    manifestOutFile.write(newFileContents, 'UTF-8')
                }
            }
        }
    }
}