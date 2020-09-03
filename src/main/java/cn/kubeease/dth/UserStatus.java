package cn.kubeease.dth;

public enum UserStatus {
    Normal("normal"),
    RemoteRemoved("remote_removed");

    private final String status;

    UserStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }
}
