package org.rwp.syncconfigfromserver.api;

import com.github.exopandora.shouldersurfing.config.Config;

public enum ConfigList{
    ShoulderSurfingClient("shouldersurfing-client.toml", Config::onConfigReload);
    private final String ConfigPath;
    private final Runnable runnable;
    ConfigList(String ConfigPath , Runnable runnable) {
        this.ConfigPath = ConfigPath;
        this.runnable = runnable;
    }
    public String getConfigName(){
        return ConfigPath;
    }
    public void runReload(){
        runnable.run();
    }

}
