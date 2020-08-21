package cn.kubeease.dth.sync;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.taobao.api.ApiException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.google.common.flogger.FluentLogger;

public class DepartmentSync extends BaseSync implements Sync {

    private static final String listURL = "https://oapi.dingtalk.com/department/list";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public DepartmentSync(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Invalid access token");
        }
        this.setAccessToken(token);

    }

    public List<OapiDepartmentListResponse.Department> download() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(listURL);
        OapiDepartmentListRequest request = new OapiDepartmentListRequest();
        request.setHttpMethod("GET");
        OapiDepartmentListResponse response = client.execute(request, this.getAccessToken());
        if (!response.isSuccess()) {
            logger.atSevere().log("get departments error: %s %s", response.getErrorCode(), response.getErrmsg());
            return new ArrayList<>();
        }
        return response.getDepartment();
    }

    public void save(List<OapiDepartmentListResponse.Department> departments) {
        if (this.getConnection() == null) {
            logger.atSevere().log("Database connection is null");
            return;
        }
        for (OapiDepartmentListResponse.Department d : departments) {
            try {
                this.saveDepartment(d);
            } catch (SQLException e) {
                logger.atWarning().withCause(e);
            }
        }

    }

    protected void saveDepartment(OapiDepartmentListResponse.Department department) throws SQLException {
        department.setParentid(department.getParentid() == null ? 0 : department.getParentid());
        PreparedStatement stmt = this.getConnection().prepareStatement("select * from department where id=?");
        stmt.setLong(1, department.getId());
        ResultSet rs = stmt.executeQuery();
        if (!rs.isBeforeFirst()) {
            PreparedStatement newStmt = this.getConnection()
                    .prepareStatement("insert into department(id,name,parentID) values(?,?,?)");

            newStmt.setLong(1, department.getId());
            newStmt.setString(2, department.getName());
            newStmt.setLong(3, department.getParentid());
            newStmt.execute();
            // if (!){
            // logger.atWarning().log("save department %d %d %s failed",
            // department.getId(),department.getParentid(),department.getName());
            // }
            newStmt.close();
        } else {
            PreparedStatement updateStmt = this.getConnection()
                    .prepareStatement("update department set name=?,parentID=? where id=?");
            updateStmt.setString(1, department.getName());
            updateStmt.setLong(2, department.getParentid());
            updateStmt.setLong(3, department.getId());
            updateStmt.execute();
            // if (!){
            // logger.atWarning().log("update department %d %d %s
            // failed.",department.getId(),department.getParentid(),department.getName());
            // }
            updateStmt.close();
        }
        stmt.close();
    }

    @Override
    public void sync() {
        try {
            this.save(this.download());
        } catch (ApiException e) {
            logger.atSevere().withCause(e);
        }

    }
}
