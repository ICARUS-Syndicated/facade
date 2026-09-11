package org.RWP.rCON;

import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.Rcon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class RCONCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rcon")) {
            if (args.length < 2) {
                sender.sendMessage("§c用法: /rcon <服务器> <命令>");
                return true; // 终止执行
            }
            else{
                String server = args[0];
                String command = args[1];
                command = command.replace("\"", "");
                for(ServerInfo serverInfo : RCON.serverList){
                    if(serverInfo.getName().equals(server)){
                        try {
                            Rcon rcon = new Rcon(serverInfo.getIp(), serverInfo.getPort(), serverInfo.getPassword());
                            RCON.logger.info("返回结果"+rcon.command(command));
                        } catch (IOException | AuthenticationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return false; // 交由其他处理器处理
    }
}
