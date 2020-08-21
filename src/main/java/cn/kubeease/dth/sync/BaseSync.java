package cn.kubeease.dth.sync;

import java.sql.Connection;

public abstract class  BaseSync {
    private String accessToken;
    private Connection connection;

    public String getAccessToken(){
        return this.accessToken;
    }
    public void setAccessToken(String token){
        this.accessToken = token;
    }

    public Connection getConnection(){
        return this.connection;
    }
    public void setConnection(Connection c){
        this.connection = c;
    }

}
