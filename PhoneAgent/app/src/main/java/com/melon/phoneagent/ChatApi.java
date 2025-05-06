package com.melon.phoneagent;

import com.melon.util.FileUtil;
import com.melon.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ChatApi 类提供了聊天功能的接口。
 * 它包含了一个静态方法 init，用于初始化聊天功能。
 * 它还包含了一个静态方法 doChat，用于执行聊天操作。
 *
 * @author Melon
 */
public class ChatApi {

    static Map<String, Globals> luaGs = new HashMap<>();

    /**
     * 初始化聊天功能。
     *
     * @param luaFiles 包含 Lua 文件的列表
     */
    static public int init(List<File> luaFiles) {
        // 遍历 Lua 文件列表
        for (File luaFile : luaFiles) {
            if (!luaFile.exists() || !luaFile.isFile()) continue;
            String fileName = luaFile.getName();
            if(fileName.isEmpty()) continue;

            String name = fileName.split("\\.")[0];

            Globals g = JsePlatform.standardGlobals();
            try {
                LuaValue chunk = g.loadfile(luaFile.getAbsolutePath());
                if(chunk == null) continue;
                chunk.call(LuaAgent.getLuaInstance());

                LuaValue func = g.get("go");
                if(func == null) continue;
                if(!func.isfunction()) continue;
            } catch (LuaError err) {
                err.printStackTrace();
                continue;
            }
            luaGs.put(name, g);
        }

        return luaGs.size();
    }

    static LuaValue toLuaVal(JSONObject jo) {

        if(jo == null || jo.length() == 0) {
            return null;
        }

        LuaValue ret = new LuaTable();

        Iterator<String> keys = jo.keys();
        while(keys.hasNext()) {
            String k = keys.next();
            if(jo.isNull(k)) {
                continue;
            }
            try {
                Object val = jo.get(k);
                if(val instanceof String) {
                    ret.set(k, (String)val);
                } else if(val instanceof Integer) {
                    ret.set(k, (Integer)val);
                } else if(val instanceof Double) {
                    ret.set(k, (Double)val);
                } else if(val instanceof Float) {
                    ret.set(k, (Float)val);
                } else {
                    ret.set(k, val.toString());
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return ret;
    }
    static public String doChat(AgentApi agentApi, String msg) {
        if(!agentApi.sendType(0, msg)) {
            return null;
        }

        JSONObject ret = null;
        while(true) {
            ret = agentApi.recvJson();
            if(ret == null) break;

            String funcName = ret.optString("func");
            if(StringUtil.isEmpty(funcName)) {
                break;
            }
//            funcName = funcName.toLowerCase();
            if(!luaGs.containsKey(funcName)) {
                break;
            }
            Globals g = luaGs.get(funcName);
            LuaValue func = g.get("go");

            JSONObject args = ret.optJSONObject("params");
            LuaValue callRet;

            try {
                LuaValue largs = toLuaVal(args);
                if (args == null || args.length() == 0) {
                    callRet = func.call();
                } else {
                    callRet = func.call(largs);
                }
                if(callRet == null) {
                    agentApi.retBuf(0, "null:");
                } else {
                    agentApi.retBuf(0, "ok:" + callRet);
                }
            } catch (LuaError error) {
                agentApi.retBuf(0, "error:" + error.getMessage());
            }
        }

        if(ret == null) {
            return null;
        }
        return ret.optString("content");
    }
}
