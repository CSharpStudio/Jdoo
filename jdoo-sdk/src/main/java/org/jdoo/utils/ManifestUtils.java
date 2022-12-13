package org.jdoo.utils;

import org.jdoo.Manifest;
import org.jdoo.exceptions.ValueException;

public class ManifestUtils {

    static Package getPackage(String module) {
        try {
            Class<?> clazz = Class.forName(module + ".package-info");
            return clazz.getPackage();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Manifest getManifest(String packageName) {
        Package pkg = getPackage(packageName);
        if (pkg == null) {
            throw new ValueException("加载模块:[" + packageName + "]失败，请检查package-info");
        }
        Manifest manifest = pkg.getAnnotation(Manifest.class);
        if (manifest == null) {
            throw new ValueException("包[" + packageName + "]未定义Manifest");
        }
        return manifest;
    }
}
