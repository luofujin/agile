package com.edreamoon

import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue
import org.gradle.api.Project

class Utils {
    static private ClassPool sClassPool = ClassPool.getDefault()
    static private def sAppendedClass = []

    static void processManifest(def filePath, Set<String> set) {
        println("@processManifest path: $filePath")
        def file = new XmlSlurper().parse(filePath)
        file.application.activity.each {
            String name = it."@android:name"
            if (name != null && name.length() > 0) {
                println("@processManifest activity: ${name}")
                set.add(name)
            }
        }
    }

    /**
     * 加入android.jar，不然找不到android相关的所有类
     * @param project
     */
    static void appendBootClassPath(Project project) {
        project.android.bootClasspath.each {
            appendClassPath(it.getAbsolutePath())
        }
    }

    /**
     * 将当前路径加入类池,不然找不到这个类
     * @param path
     */
    static void appendClassPath(String path) {
        println("@appendClassPath: ${path}")
        ClassPath classPath = sClassPool.appendClassPath(path)
        sAppendedClass.add(classPath)
    }

    /**
     * 查找注解信息
     */
    static void findAnnotatedActivities() {
        AppInfo.activities.each { activity ->
            println("@findAnnotatedActivities analysis ${activity}")

            CtClass clazz = sClassPool.get(activity)
            if (clazz.isFrozen()) {
                clazz.defrost()
            }
            AnnotationsAttribute attr = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.invisibleTag)
            if (attr != null) {
                Annotation an = attr.getAnnotation("com.edreamoon.router.annotation.Router")
                if (an != null) {
                    String value = ((StringMemberValue) an.getMemberValue("path")).getValue()
                    println("@findAnnotatedActivities activity with annotation : ${activity} -->  path(${value})")
                }
            }
        }
    }

    /**
     * 释放被占用的文件，防止clean失败
     */
    static void clean() {
        sAppendedClass.each {
            println("********** clean **********   ${it}")
            sClassPool.removeClassPath(it)
        }
        sAppendedClass.clear()
    }
}