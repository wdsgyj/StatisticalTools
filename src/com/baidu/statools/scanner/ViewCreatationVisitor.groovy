package com.baidu.statools.scanner

import com.baidu.statools.util.Tree
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
/**
 * Created by clark on 14-11-28.
 */
class ViewCreatationVisitor extends ClassFileVisitor {
    private ViewCreatationMethodVisitor methodVisitor
    def classinfo = [:]
    private Closure closure
    def methods = [:]

    ViewCreatationVisitor(Tree<String> views) {
        methodVisitor = new ViewCreatationMethodVisitor(views)
        methodVisitor { method, insnlist ->
            methods[method] = insnlist
        }
    }

    void call(Closure closure) {
        this.closure = closure
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classinfo.file = file
        classinfo.version = version
        classinfo.access = access
        classinfo.name = name
        classinfo.signature = signature
        classinfo.superName = superName
        classinfo.interfaces = interfaces
    }

    @Override
    void visitEnd() {
        if (methods.size() > 0 && closure) {
            closure.call(classinfo, methods)
            classinfo = [:]
            methods = [:]
        }
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        methodVisitor.methodinfo.access = access
        methodVisitor.methodinfo.name = name
        methodVisitor.methodinfo.desc = desc
        methodVisitor.methodinfo.signature = signature
        methodVisitor.methodinfo.exceptions = exceptions

        methodVisitor.methodinfo.superName = classinfo.superName
        methodVisitor.methodinfo.self = classinfo.name
        return methodVisitor
    }
}

class ViewCreatationMethodVisitor extends MethodVisitor {
    private Closure closure
    def methodinfo = [:]
    def list = []
    Tree<String> views

    ViewCreatationMethodVisitor(Tree<String> views) {
        super(Opcodes.ASM5)
        this.views = views
    }

    void call(Closure closure) {
        this.closure = closure
    }

    @Override
    void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKESPECIAL
                && name == '<init>'
                && views.getRandomAccessNode(owner)) {

            if (methodinfo.name == '<init>'
                    && (owner == methodinfo.superName || owner == methodinfo.self)) {
                return
            }

            list << [opcode: opcode, owner: owner, name: name, desc: desc, itf: itf]
        }
    }

    @Override
    void visitEnd() {
        if (list.size() > 0 && closure) {
            closure.call(methodinfo, list)
            methodinfo = [:]
            list = []
        }
    }
}
