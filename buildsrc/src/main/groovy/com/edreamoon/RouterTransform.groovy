package com.edreamoon

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project


class RouterTransform extends Transform {
    private Project mProject

    /**
     *  Transform中的核心方法
     * @param context
     * @param inputs 传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式
     * @param referencedInputs
     * @param outputProvider 可以获取到输出目录，最后将修改的文件复制到输出目录传递到下面的Transform
     * @param isIncremental
     * @throws IOException
     * @throws TransformException
     * @throws InterruptedException
     */

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println 'RouterTransform //=============== start ===============//'

//        //遍历input
//        inputs.each { TransformInput input ->
//            //遍历文件夹
//            input.directoryInputs.each { DirectoryInput directoryInput ->
//                //注入代码
//                ClassModifier.inject(directoryInput.file.absolutePath, mProject)
//
//                // 获取output目录
//                def dest = outputProvider.getContentLocation(directoryInput.name,
//                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)//这里写代码片
//
//                // 将input的目录复制到output指定目录
//                FileUtils.copyDirectory(directoryInput.file, dest)
//            }
//
//            ////遍历jar文件 对jar不操作，但是要输出到out路径
//            input.jarInputs.each { JarInput jarInput ->
//                //注入代码
////                ClassModifier.inject(jarInput.file.absolutePath, mProject)
//
//                // 重命名输出文件（同目录copyFile会冲突）
//                def jarName = jarInput.name
//                println("jar = " + jarInput.file.getAbsolutePath())
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
//                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                FileUtils.copyFile(jarInput.file, dest)
//            }
//        }
        def cachePath = getCachePath(mProject)
        ValueHolder.buildPath = cachePath + File.separator
        ClassModifier.appendBootClassPath(mProject)

        //Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
//                def jarName = jarInput.name
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
                ClassModifier.appendClassPath(jarInput.file.getAbsolutePath())
                //生成输出路径
//                def dest = outputProvider.getContentLocation(jarName + md5Name,
//                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
//                FileUtils.copyFile(jarInput.file, dest)

            }

            //对类型为“文件夹”的input进行遍历 文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
                ClassModifier.appendClassPath(directoryInput.file.getAbsolutePath())
                // 将input的目录复制到output指定目录
//                FileUtils.copyDirectory(directoryInput.file, dest)
//                ClassModifier.testInject(directoryInput.file.absolutePath)

            }

        }

        //查找注解信息
        ClassModifier.findAnnotatedActivities()
        ClassModifier.injectRouter(mProject)

        //Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                println("abcdef $jarName")
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //ClassModifier.appendClassPath(jarInput.file.getAbsolutePath())
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }

            //对类型为“文件夹”的input进行遍历 文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes, Format.DIRECTORY)
//                ClassModifier.appendClassPath(directoryInput.file.getAbsolutePath())
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            ClassModifier.clean()
        }

        println 'RouterTransform //=============== end===============//'
    }

    static String getCachePath(Project project) {
        String path = project.rootDir.absolutePath + File.separator + "build" + File.separator + "intermediates" + File.separator + "RouterCache"
        println("getCachePath :" + path)
        File dir = new File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.getAbsolutePath()
    }

    RouterTransform(Project project) {
        mProject = project
    }

    /**
     * @return 返回当前Transform子类的名字.该方法return出去的名字并不是最终编译时的名字。
     * gradle core的代码中可以找到Transform的管理类， com.android.build.gradle.internal.pipeline.TransformManager
     * 在此类中会根据transform返回的name按照transform${InputType1}And${InputType2}And${InputTypeN}With{name}For${flavor}${BuildType}的形式进行拼装成完成的名字
     * eg: transformClassesWith + getName() + For + Debug或Release
     */
    @Override
    String getName() {
        return getClass().simpleName
    }

    /**
     * @return 需要处理的数据类型:CLASSES和RESOURCES
     * 1. CLASSES标识要处理的数据是编译过后的java字节码，这些数据的容器可以是jar包也可以是文件夹
     * 2. RESOURCES标识要处理的是java资源
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**配置当前Transform的影响范围,相当于作用域
     * PROJECT 只处理当前项目
     * SUB_PROJECTS 只处理子项目
     * PROJECT_LOCAL_DEPS 只处理当前项目的本地依赖,例如jar, aar
     * SUB_PROJECTS_LOCAL_DEPS 只处理子项目的本地依赖,例如jar, aar
     * EXTERNAL_LIBRARIES 只处理外部的依赖库
     * PROVIDED_ONLY 只处理本地或远程以provided形式引入的依赖库
     * TESTED_CODE 测试代码
     * 跟getInputTypes方法一样, getScopes也是返回一个集合,那么就可以根据自己的需求配置多种Scope
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    //指明当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }
}