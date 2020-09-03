package cn.kubeease.dth.sync;

import cn.kubeease.dth.utils.Database;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DepartmentSyncTests {


    private final DepartmentSync sync;

    public DepartmentSyncTests() throws Exception {
        this.sync = new DepartmentSync("test token");
    }

    @BeforeEach
    public void prepare() throws SQLException {
        Database db = new Database();
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        db.setConnection(connection);
        db.migrate();
        this.sync.setConnection(connection);
    }

    @Test
    public void testSaveDepartment() {
        OapiDepartmentListResponse.Department d = new OapiDepartmentListResponse.Department();
        d.setId(1L);
        d.setName("test department");
        d.setParentid(0L);
        try {
            sync.saveDepartment(d);
        } catch (Exception e){
            Assertions.fail(e);
        }

    }


}
