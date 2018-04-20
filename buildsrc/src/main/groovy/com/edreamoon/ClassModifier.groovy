package com.edreamoon

import com.edreamoon.entity.JarEntity
import com.edreamoon.entity.TransformEntity
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

class ClassModifier {
    static private ClassPool sClassPool = ClassPool.getDefault()
    static private def sAppendedClass = []
    static private def sModifiedClass = []
    static private def ROUTER_REFER = "com.edreamoon.router.FRouter"

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
     * 将当前 类或jar 路径加入类池,不然找不到这个类
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
        ValueHolder.sActivityMapping.clear()
        ValueHolder.activities.each { activity ->
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
                    ValueHolder.sActivityMapping.put(value, activity)
                }
            }
        }
    }


    static void testInject(String path) {
        CtClass clazz, clazz1
        try {
            File dir = new File(path)
            if (dir.isDirectory()) {
                dir.eachFileRecurse { File file ->

                    String filePath = file.absolutePath
                    println("filePath = " + filePath)
                    if (file.getName().equals("PhotoActivity.class")) {

                        //获取MainActivity.class
                        CtClass ctClass = sClassPool.getCtClass("com.edreamoon.photo.PhotoActivity");
                        //解冻
                        if (ctClass.isFrozen())
                            ctClass.defrost()

                        //获取到OnCreate方法
                        CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")

                        println("方法名 = " + ctMethod)

                        String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_SHORT).show();
                                                """
                        //在方法开头插入代码
                        ctMethod.insertBefore(insetBeforeStr);
                        ctClass.writeFile(path)
                        ctClass.detach()//释放
                    }
                }
            }
        } catch (Exception e) {
            println("&&&&&&&&&&&&&&&&&&&&&&&&&&&& ${e.getMessage()}")
        } finally {
            if (null != clazz1) {
                sModifiedClass.add(clazz1)
            }
        }



        println("ClassModifier: writeRouterInfo done")
    }

    static void injectRouter(TransformEntity entity, Project project) {
        CtClass routerClass

        // Router 初始化方法代码注入
        try {
            routerClass = sClassPool.get(ROUTER_REFER)
            if (routerClass.isFrozen()) {
                routerClass.defrost()
            }

            CtMethod init = routerClass.getDeclaredMethod("init")
            ValueHolder.sActivityMapping.each {
                init.insertAfter("ACTIVITIES.put(\"${it.key}\", \"${it.value}\");")
            }
            String path = sClassPool.find(ROUTER_REFER).getFile() //routerClass.getURL().getFile()
            println("@injectRouter routerClass path: $path")
//file:/E:/code/Component/frouter/build/intermediates/intermediate-jars/google/debug/classes.jar!/com/edreamoon/router/FRouter.class
            println("@injectRouter project dir ${project.rootDir}")
            def isWindows = OperatingSystem.current().isWindows()
            def jarPath
            if (isWindows) {
                jarPath = path.replace("!/com/edreamoon/router/FRouter.class", "").split(project.rootDir.absolutePath.replaceAll("\\\\", "/"))
                println(jarPath[1])
                jarPath[1] = jarPath[1].replaceAll("/", "\\\\")
            } else {
                jarPath = path.replace("!/com/edreamoon/router/FRouter.class", "").split(project.rootDir.absolutePath)
            }

            String routerJarPath = jarPath[1]
            println("@injectRouter routerJarPath: $routerJarPath")
            for (JarEntity jarEntity in entity.jarEntities) {
                def inPath = jarEntity.inputFile.absolutePath
                println("@injectRouter jar path: ${inPath}")
                if (inPath.contains(routerJarPath)) {
                    println("find jarEntity that contains router")

                    File zipDir = new File(jarEntity.jarZipDir)
                    if (zipDir.exists()) {
                        FileUtils.deleteDirectory(zipDir)
                    }

                    List<String> list = Util.unzipJar(inPath, jarEntity.jarZipDir)
                    routerClass.writeFile(jarEntity.jarZipDir)
                    jarEntity.hasChanged = true
                    break
                }
            }
            routerClass.writeFile(ValueHolder.buildPath + "Router")
        } catch (Exception e) {
            println("error &&&&&&&&&&&&&&&&&&&&&&&&&&&& ${e.getMessage()}")
        } finally {
            if (null != routerClass) {
                sModifiedClass.add(routerClass)
            }
        }
        println("ClassModifier: writeRouterInfo done")
    }

    /**
     * 释放被占用的文件，防止clean失败
     */
    static void clean() {
        sModifiedClass.each {
            it.detach()
        }
        sModifiedClass.clear()

        sAppendedClass.each {
            sClassPool.removeClassPath(it)

        }
        sAppendedClass.clear()
    }
}