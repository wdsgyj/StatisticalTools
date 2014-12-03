package com.baidu.statools.scanner

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by clark on 14-12-1.
 */
abstract class ClassFileVisitor extends ClassVisitor {
    protected File file

    protected ClassFileVisitor() {
        super(Opcodes.ASM5)
    }

    void setFile(File f) {
        file = f
    }

    File getFile() {
        return file
    }
}
