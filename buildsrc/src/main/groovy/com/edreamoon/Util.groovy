package com.edreamoon

import com.android.annotations.Nullable

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.Adler32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Util {

    /**
     * 将该jar包解压到指定目录
     * @param jarPath jar包的绝对路径
     * @param destDirPath jar包解压后的保存路径
     * @return 返回该jar包中包含的所有class的完整类名类名集合，其中一条数据如：com.aitski.hotpatch.Xxxx.class
     */
    static List unzipJar(String jarPath, String destDirPath) {

        List list = new ArrayList()
        if (jarPath.endsWith('.jar')) {
            JarFile jarFile = new JarFile(jarPath)
            try {
                Enumeration<JarEntry> jarEntrys = jarFile.entries()
                while (jarEntrys.hasMoreElements()) {
                    JarEntry jarEntry = jarEntrys.nextElement()
                    if (jarEntry.directory) {
                        continue
                    }
                    String entryName = jarEntry.getName()
                    if (entryName.endsWith('.class')) {
                        String className = entryName.replace('\\', '.').replace('/', '.')
                        list.add(className)
                    }
                    String outFileName = destDirPath + File.separator + entryName
                    File outFile = new File(outFileName)
                    if ((outFile.getParentFile().exists() || outFile.getParentFile().mkdirs())
                            && outFile.createNewFile()) {
                        InputStream inputStream = jarFile.getInputStream(jarEntry)
                        BufferedInputStream bis = new BufferedInputStream(inputStream)
                        FileOutputStream fileOutputStream = new FileOutputStream(outFile)
                        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)
                        bos << bis
                        bos.flush()
                        bos.close()
                        bis.close()
                    }
                }
            } finally {
                jarFile.close()
            }
        }
        return list
    }

    /**
     * 重新打包jar
     * @param packagePath 将这个目录下的所有文件打包成jar
     * @param destPath 打包好的jar包的绝对路径
     */
    static boolean zipJar(String packagePath, String destPath) {

        return zip(new File(packagePath), new File(destPath))
    }

    private static boolean zip(File fileOrDirectory, File outFile) {
        //提供了一个数据项压缩成一个ZIP归档输出流
        if (!outFile.exists()) {
            if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
                return false
            }
            if (!outFile.createNewFile()) {
                return false
            }
        }
        FileOutputStream fos = new FileOutputStream(outFile, false);
        CheckedOutputStream cos = new CheckedOutputStream(fos, new Adler32());
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(cos));

        //如果此文件是一个文件，否则为false。
        if (fileOrDirectory.isFile()) {
            boolean ret = zipFileOrDirectory(zos, fileOrDirectory, "")
            zos.flush()
            zos.close()
            println("ZIP", "zip file:" + outFile.getAbsolutePath() + ", checksum:" + Long.toHexString(cos.getChecksum().getValue()));
            return ret
        } else {
            //返回一个文件或空阵列。
            File[] entries = fileOrDirectory.listFiles();
            if (null == entries || entries.length <= 0) {
                return false
            }
            boolean ret = true
            for (File entry : entries) {
                // 递归压缩，更新curPaths
                ret &= zipFileOrDirectory(zos, entry, "")
            }
            zos.flush()
            zos.close()
            println("zip file:" + outFile.getAbsolutePath() + ", checksum:" + Long.toHexString(cos.getChecksum().getValue()));
            return ret;
        }

    }


    private
    static boolean zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) {
        //如果此文件不是文件夹
        if (!fileOrDirectory.isDirectory()) {
            // 压缩文件
            byte[] buffer = new byte[2048];
            FileInputStream fis = new FileInputStream(fileOrDirectory);
            BufferedInputStream bis = new BufferedInputStream(fis, 2048);
            //实例代表一个条目内的ZIP归档
            ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
            //条目的信息写入底层流
            out.putNextEntry(entry);
            int bytes_read;
            while ((bytes_read = bis.read(buffer, 0, 2048)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            bis.close();
            out.closeEntry();
            return true;
        } else {
            // 压缩目录
            File[] entries = fileOrDirectory.listFiles();
            if (null == entries || entries.length <= 0) {
                return false;
            }
            boolean ret = true;
            for (File entry : entries) {
                // 递归压缩，更新curPaths
                ret &= zipFileOrDirectory(out, entry, curPath + fileOrDirectory.getName() + "/");
            }
            return ret;
        }
    }

    static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0
    }
}