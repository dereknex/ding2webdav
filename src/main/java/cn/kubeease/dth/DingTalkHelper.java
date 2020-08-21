package cn.kubeease.dth;

import cn.kubeease.dth.cfg.Configuration;
import cn.kubeease.dth.cfg.Reader;
import cn.kubeease.dth.sync.DepartmentSync;
import cn.kubeease.dth.sync.UserSync;
import cn.kubeease.dth.utils.Database;
import cn.kubeease.dth.webdav.WebDavClient;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.google.common.flogger.FluentLogger;
import com.taobao.api.ApiException;

import java.sql.Connection;
import java.sql.DriverManager;

public class DingTalkHelper {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public static void main(String[] args) throws Exception {
        if (args.length==0 || args[0].strip().isEmpty()){
            logger.atSevere().log("Use: java -jar dingHelper.jar config.yaml");
            System.exit(1);
        }
        Reader reader = new Reader();
        reader.read(args[0]);
        Configuration cfg = reader.getConfig();

        DingTalkHelper helper = new DingTalkHelper();
        String accessToken = helper.requestAccessToken(cfg);
        // Init Database
        Database db = new Database();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:data.db");
        db.setConnection(connection);
        db.migrate();
        // Sync departments
        DepartmentSync ds = new DepartmentSync(accessToken);
        ds.setConnection(connection);
        ds.sync();

        WebDavClient c = new WebDavClient(cfg.getWebDavHost(),cfg.getWebDavPath(),cfg.getWebDavUser(), cfg.getWebDavPassword());
        UserSync us = new UserSync(accessToken);
        us.setConnection(connection);
        us.setWebDavClient(c);
        us.sync();

    }

    protected String requestAccessToken(Configuration cfg) throws ApiException {
        DefaultDingTalkClient client = new DefaultDingTalkClient(cfg.getDingTalkURL() + "/gettoken");

        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(cfg.getAppKey());
        request.setAppsecret(cfg.getAppSecret());
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        return response.getAccessToken();
    }
}