package com.coverage

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class CoverageTransform extends Transform {

    private Project mProject;
    private String mDestDir;

    public CoverageTransform(Project p) {
        this.mProject = p;
        this.mDestDir = null;
    }

    @Override
    String getName() {
        return "CoverageTransform";
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println "================ CoverageTransform transform ================"


        //遍历input
        inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)//这里写代码片

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
                if (mDestDir == null) {
                    String path = dest.absolutePath;
                    mDestDir = path.substring(0, (path.indexOf(getName()) + getName().length()))
                    println("CoverageTransform mDestDir = " + mDestDir)
                }
            }

            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        def coverageVersion = "${mProject.version}".replace(".", "").replace("-SNAPSHOT", "")
        def buildVersion = System.getenv("BUILD_VERSION")
        def buildNumber = System.getenv("BUILD_NUMBER")
        if (buildVersion != null && buildVersion.length() > 0)
            coverageVersion = buildVersion

        println('CoverageTransform coverageVersion  ' + coverageVersion + "    mProject.rootDir = ${mProject.rootDir}")
        println("mProjectName = ${mProject.name}" + "  Project displayName = ${mProject.displayName}")

        def cmd = "java -Drevision=${coverageVersion} -DprojectName=nimo -DbuildNumber=${buildNumber} -Dfilter=${mProject.rootDir}/app/filter.txt -DshouldBanRes=true -jar ${mProject.rootDir}/app/coverageLibs/androidcov.jar ${mDestDir}"
        println('cmd' + ' = ' + cmd)

        long start = System.currentTimeMillis();
        def process = cmd.execute()
        println('CoverageTransform cmd.execute() cost : ' + (System.currentTimeMillis() - start))

        long start2 = System.currentTimeMillis();
        String text = process.text
        println('CoverageTransform print process cost : ' + (System.currentTimeMillis() - start2))
        if (text.length() <= 300) {
            println("dq-text:" + text)
        } else {
            println("dq-text2:" + text.substring(0, 300))
        }

        //FileUtils.copyFile(new File("${mProject.rootDir}/linemap.txt"), new File("${mProject.rootDir}/target/linemap.txt"))
        println("-------------- 结束 CoverageTransform----------------")

    }

    @Override
    boolean isIncremental() {
        return false
    }
}