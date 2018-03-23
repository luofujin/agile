package com.edreamoon

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlu implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("RouterPlu apply")
        project.android.registerTransform(new RouterTransform(project))
    }
}