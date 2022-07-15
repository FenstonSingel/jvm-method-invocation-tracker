package net.fennmata.fcbp.java.invoketracker

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode
import kotlin.reflect.jvm.javaMethod
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.security.ProtectionDomain

// TODO comment on the inner workings before you forget them completely
// TODO check if COMPUTE_FRAMES ClassWriter flag is not excessive
// TODO check if EXPAND_FRAMES ClassReader flag is not excessive
// TODO implement more flexible classfile debug-dumping capabilities
// TODO handle constructors with the super constructor calls differently
object MITClassFileTransformer : ClassFileTransformer {

    override fun transform(
        module: Module,
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray {
        val classfileBufferCopy = classfileBuffer.copyOf()
        val classReader = ClassReader(classfileBufferCopy)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)

        val dotSeparatedClassName = className.replace("/", ".")
        val classVisitor = MITClassVisitor(dotSeparatedClassName, classWriter)

        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

        val resultingBytecode = classWriter.toByteArray()
        val resultingBytecodeFilePath = "$dotSeparatedClassName.class"
        File(resultingBytecodeFilePath).outputStream().apply {
            write(resultingBytecode)
            close()
        }
        return resultingBytecode
    }

    private class MITClassVisitor(
        private val className: String,
        cv: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM9, cv) {

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            return MITMethodNode(mv, access, className, name, descriptor, signature, exceptions)
        }

    }

    private class MITMethodNode(
        mvDelegate: MethodVisitor,
        access: Int,
        private val className: String,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ) : MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions) {

        init {
            mv = mvDelegate
        }

        private val originalContentStartLabel = Label()
        private val finallyCodeBlockStartLabel = Label()
        private val exceptionHandlerStartLabel = Label()

        override fun visitCode() {
            super.visitCode()
            visitMethodEntryCallbackInvocation()
            super.visitLabel(originalContentStartLabel)
        }

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                super.visitJumpInsn(Opcodes.GOTO, finallyCodeBlockStartLabel)
            } else {
                super.visitInsn(opcode)
            }
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitLabel(finallyCodeBlockStartLabel)
            visitMethodExitCallbackInvocation()
            visitAppropriateReturnInsn()

            super.visitLabel(exceptionHandlerStartLabel)
            visitMethodExitCallbackInvocation()
            super.visitInsn(Opcodes.ATHROW)

            super.visitMaxs(maxStack, maxLocals)
        }

        override fun visitEnd() {
            super.visitEnd()
            val tryCatchBlockNode = TryCatchBlockNode(
                getLabelNode(originalContentStartLabel),
                getLabelNode(finallyCodeBlockStartLabel),
                getLabelNode(exceptionHandlerStartLabel),
                null
            )
            tryCatchBlocks.add(tryCatchBlockNode)
            accept(mv)
        }

        private fun visitAppropriateReturnInsn() {
            val appropriateReturnInsn = when (Type.getReturnType(desc)) {
                Type.VOID_TYPE -> Opcodes.RETURN
                Type.BOOLEAN_TYPE,
                Type.CHAR_TYPE,
                Type.BYTE_TYPE,
                Type.SHORT_TYPE,
                Type.INT_TYPE -> Opcodes.IRETURN
                Type.LONG_TYPE -> Opcodes.LRETURN
                Type.FLOAT_TYPE -> Opcodes.FRETURN
                Type.DOUBLE_TYPE -> Opcodes.DRETURN
                else -> Opcodes.ARETURN
            }
            super.visitInsn(appropriateReturnInsn)
        }

        private fun visitMethodInvocationInsn(method: Method) {
            check(Modifier.isStatic(method.modifiers)) { "Non-static methods aren't supported" }
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                method.declaringClass.internalName,
                method.name,
                Type.getMethodDescriptor(method),
                false
            )
        }

        private fun visitMethodEntryCallbackInvocation() {
            super.visitLdcInsn("$className.$name$desc")
            visitMethodInvocationInsn(checkNotNull(MethodInvocationTracker::onMethodEntry.javaMethod))
        }

        private fun visitMethodExitCallbackInvocation() {
            super.visitLdcInsn("$className.$name$desc")
            visitMethodInvocationInsn(checkNotNull(MethodInvocationTracker::onMethodExit.javaMethod))
        }

    }

}
