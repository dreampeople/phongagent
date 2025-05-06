package com.melon.phoneagent;

import com.melon.util.APKUtil;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.InputStream;

/**
 * Created by melon on 2017/12/19.
 * @author Melon
 * @version 1.0
 * @since 1.0
 * @see LuaValue
 */
public class LuaAgent implements ResourceFinder {

    static LuaAgent agent =new LuaAgent();

    public static LuaValue getLuaInstance() {
        return CoerceJavaToLua.coerce(agent);
    }

    @Override
    public InputStream findResource(String filename) {
        return null;
    }

    public Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Class<APKUtil> getAPKUtil() {
        return APKUtil.class;
    }
}
