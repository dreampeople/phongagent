import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// 模拟 maconn 类
class Maconn {
    public static CompletableFuture<Void> sendType(Socket conn, int type, String... data) throws IOException {
        return CompletableFuture.runAsync(() -> {
            try {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(type);
                if (data.length > 0) {
                    byte[] dataBytes = data[0].getBytes(StandardCharsets.UTF_8);
                    outputStream.write(dataBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static CompletableFuture<byte[]> recvBufWithVerify(Socket conn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputStream = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                byte[] result = new byte[bytesRead];
                System.arraycopy(buffer, 0, result, 0, bytesRead);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return new byte[0];
            }
        });
    }

    public static CompletableFuture<Socket> connectServer(String host, int port) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new Socket(host, port);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}

// 模拟 mutil 类
class Mutil {
    public static String getStringMd5(byte[] data) {
        // 这里只是简单模拟，实际需要实现 MD5 计算
        return "md5_result";
    }
}

public class Test {
    public static CompletableFuture<Void> doChat(Socket conn) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("doChat...........");
            try {
                Maconn.sendType(conn, 0, "my name is hello").get();
                byte[] buf = Maconn.recvBufWithVerify(conn).get();
                String decodedBuf = new String(buf, StandardCharsets.UTF_8);
                System.out.println(decodedBuf);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static CompletableFuture<Map<String, String>> doListLua(Socket conn) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Maconn.sendType(conn, 10).get();
                byte[] buf = Maconn.recvBufWithVerify(conn).get();
                String jsonStr = new String(buf, StandardCharsets.UTF_8);
                // 这里简单模拟 JSON 解析，实际需要使用 JSON 库
                Map<String, String> ret = new HashMap<>();
                ret.put("example", "value");
                System.out.println(ret);
                return ret;
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        });
    }

    public static CompletableFuture<Void> doGetLuaFile(Socket conn, String fileName, String m5) {
        return CompletableFuture.runAsync(() -> {
            try {
                Maconn.sendType(conn, 11, fileName).get();
                byte[] buf = Maconn.recvBufWithVerify(conn).get();
                String md5 = Mutil.getStringMd5(buf);
                System.out.println(md5 + " " + m5);
                System.out.println(new String(buf, StandardCharsets.UTF_8));
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        try {
            Socket conn = Maconn.connectServer("127.0.0.1", 9939).get();
            // doChat(conn).get();
            Map<String, String> j = doListLua(conn).get();
            for (Map.Entry<String, String> entry : j.entrySet()) {
                String name = entry.getKey();
                String m5 = entry.getValue();
                doGetLuaFile(conn, name, m5).get();
            }
            conn.close();
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }
}