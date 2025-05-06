package com.melon.phoneagent;

import com.melon.util.AndroidUtil;
import com.melon.util.FileUtil;
import com.melon.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AgentApi 类提供了与服务器进行通信的一系列方法，包括连接服务器、发送消息、接收数据等功能。
 * 它封装了与服务器通信的底层逻辑，使用户可以方便地进行各种操作。
 */
public class AgentApi {
    final String serverIp = "47.96.249.247";
//    final String serverIp = "192.168.2.182"; // 服务器的 IP 地址;
//    final String serverIp = "192.168.0.83"; // 服务器的 IP 地址
    final int serverPort = 9939; // 服务器的端口号
    final File luaDir = FileUtil.getLocalSDDir("luafiles"); // Lua 文件存储目录
    List<File> luaFiles = new ArrayList<>(); // Lua 文件列表

    /**
     * 回调接口，用于在异步操作完成时接收结果。
     * 实现该接口的类可以在异步操作完成时执行相应的操作。
     */
    static class Callback {
        void doChatFinished(String msg) {}
        void doInitFinished(boolean ret) {}
    };

    Socket mConn = null; // 与服务器的连接
    public int mRet = 0; // 操作结果代码
    public String mErrMsg = null; // 操作错误信息

    static AgentApi mInstance = null; // 单例实例
    /**
     * 获取 AgentApi 类的单例实例。
     *
     * @return AgentApi 类的单例实例
     */
    public static AgentApi getInstance() {
        if(mInstance == null) {
            mInstance = new AgentApi();
        }
        return mInstance;
    }

    /**
     * 连接到指定的服务器。
     *
     * @return 如果连接成功则返回 true，否则返回 false
     */
    private boolean doInit_() {
        luaFiles.clear();

        JSONArray array = doListLua();
        if(array == null) return false;

        for(int ii=0;ii<array.length();ii++) {
            JSONObject o = array.optJSONObject(ii);
            if(o == null) continue;

            String name = o.optString("name");
            String m5 = o.optString("md5");
            if(StringUtil.isEmpty(name) || StringUtil.isEmpty(m5)) {
                continue;
            }

            if(!doGetLuaFile(name, m5)) {
                return false;
            }
        }

        ChatApi.init(luaFiles);
        return true;
    }

    /**
     * 执行初始化操作，连接到服务器并执行初始化操作。
     * @param callback
     */
    private void doInit(Callback callback) {
        if(!connectServer(serverIp, serverPort)) {
            if(callback != null) {
                callback.doInitFinished(false);
                return;
            }
        }
        boolean ret = doInit_();
        close();

        if(callback != null) {
            callback.doInitFinished(ret);
        }
    }

    /**
     * 执行初始化操作，连接到服务器并执行初始化操作。
     * @param callback
     */
    public void init(Callback callback) {
        if(!AndroidUtil.isMainThread()) {
            doInit(callback);
            return;
        }

        new Thread(() -> doInit(callback)).start();
    }

    /**
     * 执行聊天操作，向服务器发送聊天消息并接收响应。
     *
     * @param msg 要发送的聊天消息
     * @return 服务器返回的响应字符串，如果发送或接收失败则返回 null
     */
    private void doChat(String msg, Callback callback) {
        if(!connectServer(serverIp, serverPort)) {
            if(callback != null) {
                callback.doChatFinished(null);
                return;
            }
        }
        String ret = ChatApi.doChat(this, msg);
        close();
        if(callback != null) {
            callback.doChatFinished(ret);
        }
    }

    /**
     * 执行聊天操作，向服务器发送聊天消息并接收响应。
     *
     * @param msg 要发送的聊天消息
     */
    public void chat(String msg, Callback callback) {
        if(!AndroidUtil.isMainThread()) {
            doChat(msg, callback);
            return;
        }
        new Thread(() -> doChat(msg, callback)).start();
    }

    /**
     * 获取 Lua 文件列表，向服务器发送请求并接收响应。
     *
     * @return 服务器返回的 Lua 文件列表的 JSON 数组，如果发送或接收失败则返回 null
     */
    private JSONArray doListLua() {
        if(!sendType(10)) {
            return null;
        }

        JSONObject ret = recvJson();
        if(ret == null) return null;

        return ret.optJSONArray("luaList");
    }

    /**
     * 获取指定名称和 MD5 值的 Lua 文件，向服务器发送请求并接收响应。
     *
     * @param fileName Lua 文件的名称
     * @param md5      Lua 文件的 MD5 值
     * @return 如果成功获取文件则返回 true，否则返回 false
     */
    private boolean doGetLuaFile(String fileName, String md5) {
        if(!sendType(11, fileName)) {
            return false;
        }
        
        byte[] ret = recvBufWithVerify();
        if(ret == null) return false;

        String m5 = StringUtil.getMd5(ret);
        if(!m5.equalsIgnoreCase(md5)) {
            return false;
        }

        if(!luaDir.exists()) {
            luaDir.mkdirs();
        } else {
            FileUtil.clearDir(luaDir);
        }
        File file = new File(luaDir, fileName + ".lua");
        FileUtil.writeFile(file, ret);

        luaFiles.add(file);

        return true;
    }

    /**
     * 连接到指定 IP 地址和端口的服务器。
     *
     * @param ip   服务器的 IP 地址
     * @param port 服务器的端口号
     * @return 如果连接成功则返回 true，否则返回 false
     */
    public boolean connectServer(String ip, int port) {
        if(mConn != null && mConn.isConnected()) {
            return true;
        }
        try {
            mConn = new Socket();
            // 这里可以添加更多的连接成功后的处理逻辑
            mConn.connect(new InetSocketAddress(ip, port), 3000);
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            mConn = null;
            return false;
        }
    }

    /**
     * 关闭与服务器的连接。
     */
    public void close() {
        if(mConn == null) return;
        try {
            mConn.close();
        } catch (IOException ignored) {
        }
        mConn = null;
    }

    boolean retBuf(int retVal, String msg) {
        if(mConn == null || !mConn.isConnected()) {
            mConn = null;
            return false;
        }

        // 假设我们使用 DataOutputStream 来将 sendType 和 msg 写入一个包体
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            int len = 8;
            byte[] msgbuf = null;
            if(msg != null) {
                msgbuf = msg.getBytes();
                len += msgbuf.length;
            }
            dos.writeInt(len);
            dos.writeInt(retVal);
            if(msgbuf != null) {
                dos.write(msgbuf);
            }
            byte[] packet = bos.toByteArray();
            // 现在 packet 就是包含 sendType 和 msg 的包体
            // 后续可以将 packet 发送到输出流
            OutputStream out = mConn.getOutputStream();
            out.write(packet);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向服务器发送指定类型和消息的数据包。
     *
     * @param type 数据包的类型
     * @param msg  要发送的消息
     * @return 如果发送成功则返回 true，否则返回 false
     */
    boolean sendType(int type, String msg) {
        if(mConn == null || !mConn.isConnected()) {
            mConn = null;
            return false;
        }

        // 假设我们使用 DataOutputStream 来将 sendType 和 msg 写入一个包体
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            int len = 6;
            byte[] msgbuf = null;
            if(msg != null) {
                msgbuf = msg.getBytes();
                len += msgbuf.length;
            }
            dos.writeInt(len);
            dos.writeShort(type);
            if(msgbuf != null) {
                dos.write(msgbuf);
            }
            byte[] packet = bos.toByteArray();
            // 现在 packet 就是包含 sendType 和 msg 的包体
            // 后续可以将 packet 发送到输出流
            OutputStream out = mConn.getOutputStream();
            out.write(packet);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向服务器发送指定类型的数据包，消息为空。
     *
     * @param type 数据包的类型
     * @return 如果发送成功则返回 true，否则返回 false
     */
    boolean sendType(int type) {
        return sendType(type, null);
    }

    /**
     * 从服务器接收数据包。
     *
     * @return 接收到的数据包字节数组，如果接收失败则返回 null
     */
    byte[] recvBuf() {
        if(mConn == null || !mConn.isConnected()) {
            mConn = null;
            return null;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataInputStream dis = new DataInputStream(mConn.getInputStream());
            int len = dis.readInt() - 4;
            if(len < 0 || len > 1024 * 1024) {
                return null;
            }
            byte[] buf = new byte[len];
            int readLen = 0; // 已读取的字节数
            while (readLen < len) {
                readLen += dis.read(buf, readLen, len - readLen);
            }
            bos.write(buf);
            // 后续可以将 packet 发送到输出流
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从服务器接收数据包并进行验证。
     *
     * @return 验证后的数据包字节数组，如果接收或验证失败则返回 null
     */
    byte[] recvBufWithVerify() {
        byte[] recvBuf = recvBuf();
        if(recvBuf == null) {
            mRet = -1;
            mErrMsg = "recv error![-1]";
            return null;
        }
        if(recvBuf.length < 4) {
            mRet = -1;
            mErrMsg = "recv error![0]";
            return null;
        }

        // 解包操作
        DataInputStream unpackage = new DataInputStream(new ByteArrayInputStream(recvBuf));
        try {
            mRet = unpackage.readInt();
            // 跳过前4个字节
            byte[] remainingData = new byte[recvBuf.length - 4];
            System.arraycopy(recvBuf, 4, remainingData, 0, remainingData.length);

            if(mRet != 0) {
                String errmsg = new String(remainingData, StandardCharsets.UTF_8);
                try {
                    JSONObject o = new JSONObject(errmsg);
                    mErrMsg = o.optString("errmsg");
                } catch (JSONException e) {
                    mErrMsg = errmsg;
                }
                return null;
            }

            mErrMsg = null;
            return remainingData;
        } catch (IOException e) {
            mRet = -1;
            mErrMsg = "recv error![1]";
            return null;
        }
    }

    /**
     * 从服务器接收 JSON 数据。
     *
     * @return 接收到的 JSON 对象，如果接收或解析失败则返回 null
     */
    JSONObject recvJson() {
        byte[] recvBuf = recvBufWithVerify();
        if(recvBuf == null) return null;

        try {
            return new JSONObject(new String(recvBuf, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            mRet = -1;
            mErrMsg = e.toString();
            return null;
        }
    }
}
