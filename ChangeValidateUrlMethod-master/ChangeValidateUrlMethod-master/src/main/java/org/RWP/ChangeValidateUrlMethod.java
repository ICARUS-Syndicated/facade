package org.rwp;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * Java Agent that strips the host validation check from
 * CraftPlayerTextures.validateTextureUrl(URL).
 * <p>
 * The original method:
 *   private static void validateTextureUrl(@Nullable URL url) {
 *       if (url == null) return;
 *       Preconditions.checkArgument(url.getHost().equals(MINECRAFT_HOST), ...);
 *   }
 * <p>
 * After transformation: does nothing (empty method body).
 */
public class ChangeValidateUrlMethod {

    private static final String TARGET_CLASS =
            "org/bukkit/craftbukkit/profile/CraftPlayerTextures";

    private static final String TARGET_METHOD = "validateTextureUrl";

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[RWP] Agent loaded. Using ASM tree API.");

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) {
                if (classfileBuffer == null) return null;
                if (!TARGET_CLASS.equals(className)) return null;

                try {
                    System.out.println("[RWP] Transforming " + className.replace('/', '.'));
                    return patchValidateTextureUrl(classfileBuffer);
                } catch (Exception e) {
                    System.err.println("[RWP] Failed to transform: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    private static byte[] patchValidateTextureUrl(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        boolean found = false;

        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            if (TARGET_METHOD.equals(mn.name)) {
                found = true;
                System.out.println("[RWP]   Found method: " + mn.name + mn.desc);

                // Replace method body with empty: just a return
                mn.instructions.clear();
                mn.instructions.add(new InsnNode(Opcodes.RETURN));
                mn.maxStack = 0;
                mn.maxLocals = 1;

                // Remove try-catch blocks
                mn.tryCatchBlocks.clear();

                System.out.println("[RWP]   Method body replaced with empty return.");
                break;
            }
        }

        if (!found) {
            System.out.println("[RWP]   WARNING: Method '" + TARGET_METHOD + "' not found!");
            return null;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        byte[] result = cw.toByteArray();
        System.out.println("[RWP]   Transformed bytes: " + result.length +
                " (original: " + classBytes.length + ")");
        return result;
    }
}
