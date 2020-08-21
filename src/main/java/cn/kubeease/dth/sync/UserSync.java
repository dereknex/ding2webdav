package cn.kubeease.dth.sync;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import cn.kubeease.dth.webdav.WebDavClient;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserListbypageRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiUserListbypageResponse;
import com.google.common.flogger.FluentLogger;
import com.taobao.api.ApiException;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Organization;
import ezvcard.property.Profile;
import ezvcard.property.VCardProperty;

public class UserSync extends BaseSync implements Sync {

    private static final String listURL = "https://oapi.dingtalk.com/user/listbypage";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final DingTalkClient client;
    private WebDavClient webDavClient;

    public void setWebDavClient(WebDavClient c){
        this.webDavClient = c;
    }

    public UserSync(final String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Invalid access token");
        }
        this.setAccessToken(token);
        this.client = new DefaultDingTalkClient(listURL);
    }

    protected List<OapiUserListbypageResponse.Userlist> downloadWithPage(final long departmentID, final long offset,
            final long size) throws ApiException {
        final OapiUserListbypageRequest request = new OapiUserListbypageRequest();
        request.setDepartmentId(departmentID);
        request.setOffset(offset);
        request.setHttpMethod("GET");
        request.setSize(size);
        final OapiUserListbypageResponse response = client.execute(request, this.getAccessToken());
        return response.getUserlist();
    }

    public void saveUsers(final List<OapiUserListbypageResponse.Userlist> users, final String departmentName) {
        for (final OapiUserListbypageResponse.Userlist u : users) {
            this.saveUser(u, departmentName);
            try {
                this.webDavClient.uploadCard(this.createUserCard(u));
            } catch (IOException e) {
                logger.atSevere().withCause(e);
            }
        }
    }

    public void saveUser(final OapiUserListbypageResponse.Userlist user, final String departmentName) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.getConnection().prepareStatement("select * from user where id=?");
            stmt.setString(1, user.getUserid());
            rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                this.createUser(user, departmentName);
            } else {
                this.updateUser(user, departmentName);
            }

        } catch (final SQLException e) {
            logger.atSevere().withCause(e);
        } finally {

            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null)
                    rs.close();
            } catch (final SQLException e) {
                logger.atSevere().withCause(e);
            }

        }

    }

    protected void updateUser(final OapiUserListbypageResponse.Userlist user, final String departmentName) {
        PreparedStatement stmt = null;
        try {
            final String sql = "update user user set avator=?, department=?, email=?, hiredDate=?, mobile=?, name=?, active=?, jobNumber=?, orgEmail=?, position=?, remark=?, tel=?, workplace=? where id=?";
            stmt = this.getConnection().prepareStatement(sql);

            stmt.setString(1, user.getAvatar());
            stmt.setString(2, departmentName);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getHiredDate()==null?"":user.getHiredDate().toString());
            stmt.setString(5, user.getMobile());
            stmt.setString(6, user.getName());
            stmt.setInt(7, user.getActive()?1:0);
            stmt.setString(8, user.getJobnumber());
            stmt.setString(9, user.getOrgEmail());
            stmt.setString(10, user.getPosition());
            stmt.setString(11, user.getRemark());
            stmt.setString(12, user.getTel());
            stmt.setString(13, user.getWorkPlace());
            stmt.setString(14, user.getUserid());
            stmt.execute();
        } catch (final SQLException e) {
            logger.atSevere().withCause(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                logger.atSevere().withCause(e);
            }
        }
    }

    protected void createUser(final OapiUserListbypageResponse.Userlist user, final String departmentName) {
        PreparedStatement newStmt = null;
        try {
            final String sql = "insert into user values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            newStmt = this.getConnection().prepareStatement(sql);
            newStmt.setString(1, user.getUserid());
            newStmt.setString(2, user.getAvatar());
            newStmt.setString(3, departmentName);
            newStmt.setString(4, user.getEmail());
            newStmt.setString(5, user.getHiredDate()==null?"":user.getHiredDate().toString());
            newStmt.setString(6, user.getMobile());
            newStmt.setString(7, user.getName());
            newStmt.setInt(8, user.getActive()?1:0);
            newStmt.setString(9, user.getJobnumber());
            newStmt.setString(10, user.getOrgEmail());
            newStmt.setString(11, user.getPosition());
            newStmt.setString(12, user.getRemark());
            newStmt.setString(13, user.getTel());
            newStmt.setString(14, user.getWorkPlace());
            newStmt.execute();
        } catch (final SQLException e) {
            logger.atSevere().withCause(e);
        } finally {
            try {
                if (newStmt != null) {
                    newStmt.close();
                }
            } catch (final SQLException e) {
                logger.atSevere().withCause(e);
            }
        }
    }

    protected List<OapiDepartmentListResponse.Department> queryDepartments() {
        final List<OapiDepartmentListResponse.Department> departments = new ArrayList<>();
        final Connection conn = this.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select id, name from department");
            while (rs.next()) {
                final OapiDepartmentListResponse.Department d = new OapiDepartmentListResponse.Department();
                d.setId(rs.getLong(1));
                d.setName(rs.getString(2));
                departments.add(d);
            }
        } catch (final SQLException e) {
            logger.atSevere().withCause(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
            }catch (SQLException e){
                logger.atSevere().withCause(e);
            }
        }
        return departments;

    }

    public VCard createUserCard(OapiUserListbypageResponse.Userlist user){
        VCard vcard = new VCard();
        vcard.setFormattedName(user.getName());
        vcard.setExtendedProperty("userID",user.getUserid());
        vcard.addEmail(user.getEmail(), EmailType.PREF);
        vcard.addEmail(user.getOrgEmail(), EmailType.WORK);
        vcard.addTelephoneNumber(user.getMobile(), TelephoneType.VOICE);
        vcard.addTelephoneNumber(user.getTel(), TelephoneType.WORK);
        vcard.addTitle(user.getPosition());
        vcard.setExtendedProperty("jobNumber",user.getJobnumber());
        return vcard;
    }

    @Override
    public void sync() {
        final List<OapiDepartmentListResponse.Department> departments = this.queryDepartments();
        for (final OapiDepartmentListResponse.Department d : departments) {
            long offset = 0L;
            final long size = 20L;
            try {
                while (true) {
                    final List<OapiUserListbypageResponse.Userlist> users = this.downloadWithPage(d.getId(), offset,
                            size);
                    this.saveUsers(users, d.getName());
                    if (users.size() < size) {
                        break;
                    }
                    offset += size;
                }
            } catch (final ApiException e) {
                logger.atSevere().withCause(e);
                break;
            }
        }
    }
}
