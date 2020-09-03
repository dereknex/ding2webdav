package cn.kubeease.dth.cfg;

public final class Configuration {
    private String corpID;
    private String appKey;
    private String appSecret;
    private String dingTalkURL;
    private String webDavHost;
    private String webDavPath;
    private String webDavUser;
    private String webDavPassword;
    private String dataPath;

    public String getCorpID() {
        return this.corpID;
    }

    public void setCorpID(String corpID) {
        this.corpID = corpID;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public void setAppKey(String key) {
        this.appKey = key;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public void setAppSecret(String secret) {
        this.appSecret = secret;
    }

    public String getDingTalkURL() {
        return this.dingTalkURL;
    }

    public void setDingTalkURL(String url) {
        this.dingTalkURL = url;
    }

    public String getWebDavHost() {
        return this.webDavHost;
    }

    public void setWebDavHost(String host) {
        this.webDavHost = host;
    }

    public String getWebDavPath() {
        return this.webDavPath;
    }

    public void setWebDavPath(String path) {
        this.webDavPath = path;
    }

    public String getWebDavUser() {
        return this.webDavUser;
    }

    public void setWebDavUser(String user) {
        this.webDavUser = user;
    }

    public String getWebDavPassword() {
        return this.webDavPassword;
    }

    public void setWebDavPassword(String password) {
        this.webDavPassword = password;
    }

    public String getDataPath() { return this.dataPath;}
    public void setDataPath(String path) { this.dataPath = path;}

}
