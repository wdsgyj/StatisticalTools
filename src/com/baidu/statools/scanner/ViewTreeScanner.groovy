package com.baidu.statools.scanner
import com.baidu.statools.util.Tree
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
/**
 * Created by clark on 14-11-28.
 */
class ViewTreeScanner {
    private static final boolean DEBUG = false
    List<File> files

    static void main(String[] args) {
        ViewTreeScanner scanner = new ViewTreeScanner(
                files: ['/home/clark/programe/android-sdk-linux/platforms/android-21/android.jar' as File,
                        '/home/clark/dev/android/clark/AndroidStat/bin/classes' as File]
        )
        Tree<String> viewTree = scanner.findViewTree()
        def rs = scanner.findAllViewsCreatation(viewTree)
        rs.entrySet().each { entry ->
            def classinfo = entry.key
            def methods = entry.value
            println("${classinfo.file.getAbsolutePath()} $classinfo.name")
            methods.each { methodentry ->
                def methodinfo = methodentry.key
                def insnlist = methodentry.value
                println("\t$methodinfo.name $methodinfo.desc")
                insnlist.each { insn ->
                    println("\t\tnew $insn.owner $insn.desc")
                }
            }
        }
    }

    Tree<String> findViewTree() {
        Tree<String> result = new Tree<>("java/lang/Object")
        Map<String, Tree<String>> templeTrees = new HashMap<>()
        Set<String> processed = new HashSet<>()

        def visitor = new ViewsClassVisitor()
        visitor { String name, String parent, boolean isInterface, boolean isAbstract ->
            if (name in processed) {
                if (DEBUG) {
                    System.err.println("已经处理过 $name")
                }
                return
            } else {
                processed << name
            }

            Tree<String> currentClass = templeTrees.remove(name)
            if (currentClass) {
//                currentClass.value.isInterface = isInterface
//                currentClass.value.isAbstract = isAbstract
            } else {
//                name.isInterface = isInterface
//                name.isAbstract = isAbstract
                currentClass = new Tree<>(name)
                if (DEBUG) {
                    println("发现新节点 $name 继承自 $parent")
                }
            }

            Tree<String> parentClass = result.getRandomAccessNode(parent)
            if (parentClass) {
                if (DEBUG) {
                    println("在主树中找到 $parent 将当前节点加进去")
                }
                parentClass.addChild(currentClass)
            } else {
                def r = templeTrees.values().find { Tree<String> root ->
                    parentClass = root.getRandomAccessNode(parent)
                    if (parentClass) {
                        if (DEBUG) {
                            println("在临时树中找到 $parent 将当前节点加进去")
                        }
                        parentClass.addChild(currentClass)
                        return true
                    }

                    return false
                }

                if (!r) {
                    if (DEBUG) {
                        println("创建新的临时树 $parent 将当前节点加进去")
                    }
                    parentClass = new Tree<>(parent)
                    parentClass.addChild(currentClass)
                    templeTrees[parent] = parentClass
                }
            }

            if (DEBUG) {
                println()
            }
        }
        findViewinfos(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)

        if (templeTrees.size() > 0) {
            println("仍然有找不到 parent 的类型")
            templeTrees.values().each {
                println("\t$it")
            }
        }
        processed.clear()
        templeTrees.clear()

        return result.getRandomAccessNode("android/view/View")
    }

    def findAllViewsCreatation(Tree<String> tree) {
        def visitor = new ViewCreatationVisitor(tree)
        def res = [:]
        visitor { classinfo, methods ->
            if (classinfo.name.startsWith('android')
                    || classinfo.name.startsWith('com/unionpay')
                    || classinfo.name.startsWith('com/UCMobile/PayPlugin')
                    || classinfo.name.startsWith('com/baidu/navisdk')
                    || classinfo.name.startsWith('com/google')
                    || classinfo.name.startsWith('com/baidu/android/lbspay')
                    || classinfo.name.startsWith('com/baidu/android/pay')
                    || classinfo.name.startsWith('com/baidu/wallet')
                    || classinfo.name.startsWith('com/weibo')
                    || classinfo.name.startsWith('com/baidu/sapi2')) {
                return
            }
            res[classinfo] = methods
        }
        findViewinfos(visitor, 0)
        return res
    }

    private void findViewinfos(ClassFileVisitor visitor, int flag) {
        if (!files) {
            return
        }

        LinkedList<File> fileList = new LinkedList(files)
        while (fileList.size() > 0) {
            File f = fileList.removeFirst()
            if (!f) {
                continue
            }

            if (f.isFile()) {
                String name = f.name
                if (name.endsWith(".class")) {
                    processInputStream(f, new FileInputStream(f), visitor, flag)
                } else if (name.endsWith(".zip")
                        || name.endsWith(".war")
                        || name.endsWith(".apk")
                        || name.endsWith("jar")) {
                    processZipFile(f, visitor, flag)
                }
            } else if (f.isDirectory()) {
                File[] children = f.listFiles()
                if (children) {
                    children.each { child ->
                        fileList.addLast(child)
                    }
                }
            }
        }
    }

    private void processZipFile(File file, ClassFileVisitor visitor, int flag) {
        ZipFile zipFile = new ZipFile(file)
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.name
                if (name.endsWith(".class")) {
                    processInputStream(file, zipFile.getInputStream(zipEntry), visitor, flag)
                } else {
//                    println("发现不能处理的压缩文件 $name")
                }
            }
        } finally {
            IOUtils.closeQuietly(zipFile)
        }
    }

    private void processInputStream(File file, InputStream inputStream, ClassFileVisitor visitor, int flag) {
        try {
            ClassReader reader = new ClassReader(inputStream)
            visitor.setFile(file)
            reader.accept(visitor, flag)
        } finally {
            IOUtils.closeQuietly(inputStream)
        }
    }
}
