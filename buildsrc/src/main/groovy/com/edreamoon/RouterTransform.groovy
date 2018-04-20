package com.edreamoon

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.edreamoon.entity.DirectoryEntity
import com.edreamoon.entity.JarEntity
import com.edreamoon.entity.TransformEntity
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
        try {
            def cachePath = getCachePath(mProject)
            ValueHolder.buildPath = cachePath + File.separator
            ClassModifier.appendBootClassPath(mProject)

            TransformEntity entity = new TransformEntity()
            //Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
            inputs.each { TransformInput input ->
                //对类型为“文件夹”的input进行遍历 文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    ClassModifier.appendClassPath(directoryInput.file.getAbsolutePath())
                    entity.directoryEntities.add(new DirectoryEntity(directoryInput.file, dest))
                    println("DirectoryInput each: ${directoryInput.file} --- ${dest}")
                    //\app\build\intermediates\classes\prod\debug --- \app\build\intermediates\transforms\RouterTransform\prod\debug\0
                }

                //对类型为jar文件的input进行遍历， 一般是第三方依赖库jar文件，lib类型的module也以jar的形式处理,从下面输出可以看出
                input.jarInputs.each { JarInput jarInput ->
                    ClassModifier.appendClassPath(jarInput.file.getAbsolutePath())

                    // 重命名输出文件（同目录copyFile会冲突）
                    def jarName = jarInput.file.name
                    def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
                    File dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    println("JarInputs each: ${jarInput.file} --- ${dest}")
                    //\mphoto\build\intermediates\intermediate-jars\prod\debug\classes.jar --- \app\build\intermediates\transforms\RouterTransform\prod\debug\33.jar

                    String jarZipDir = context.getTemporaryDir().getAbsolutePath() + File.separator + jarName.replace(".jar", "") + md5Name
                    def jarEntity = new JarEntity(jarInput.file, dest, jarZipDir,)
                    entity.jarEntities.add(jarEntity)
                }
            }

            //查找注解信息
            ClassModifier.findAnnotatedActivities()
            ClassModifier.injectRouter(entity, mProject)

            entity.directoryEntities.each {
                FileUtils.copyDirectory(it.inputFile, it.outputFile)
                println("directory copy: ${it.inputFile} to ${it.outputFile}")
            }

            entity.jarEntities.each { JarEntity it ->
                if (it.hasChanged) {
                    String path = it.outputFile.absolutePath.replace(".jar", ".temp")
                    println("***********bef")
                    boolean jar = Util.zipJar(it.jarZipDir, path)
                    println("***********after $jar   ${new File(path).length()}")

                    it.inputFile = new File(path)
                    println("***********333")

                }

                //将输入内容复制到输出
                if (it.outputFile.exists() && (it.outputFile.lastModified() != it.inputFile.lastModified() || it.outputFile.length() != it.inputFile.length())) {
                    it.outputFile.delete()
                    println("output file exist and not same with input file , delete output file ${it.outputFile.absolutePath}")
                }
                if (!it.outputFile.exists()) {
                    FileUtils.copyFile(it.inputFile, it.outputFile)
                    println("copy jar file  from ${it.inputFile.absolutePath} to ${it.outputFile.absolutePath}")
                }
                it.inputFile.delete()
//                FileUtils.copyFile(it.inputFile,it.outputFile)
            }
        } finally {
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