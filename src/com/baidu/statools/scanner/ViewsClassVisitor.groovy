package com.baidu.statools.scanner

import org.objectweb.asm.Opcodes
/**
 * Created by clark on 14-11-28.
 */
class ViewsClassVisitor extends ClassFileVisitor {
    private boolean skip
    private Closure callback
    private String name
    private String parentName
    private boolean isInterface
    private boolean isAbstract

    ViewsClassVisitor() {
    }

    void call(Closure closure) {
        callback = closure
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // 忽略 java.lang.Object 类
        if (!superName) {
            skip = true
        }

        this.name = name
        this.parentName = superName
        isInterface = (access & Opcodes.ACC_INTERFACE) != 0
        isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0
    }

    @Override
    void visitEnd() {
        if (skip) {
            skip = false
            return
        }
        if (callback) {
            callback.call(name, parentName, isInterface, isAbstract)
        }
    }
}
