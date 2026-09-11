package org.RWP.rCON;

public class ServerInfo {
    private String name;
    private String ip;
    private int port;
    private String password;

    // 构造函数
    public ServerInfo(String name, String ip, int port, String password) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.password = password;
    }

    // Getter 方法
    public String getName() { return name; }
    public String getIp() { return ip; }
    public int getPort() { return port; }
    public String getPassword() { return password; }
}
