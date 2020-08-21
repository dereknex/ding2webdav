package cn.kubeease.dth.utils;

import com.google.common.flogger.FluentLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class Database {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Connection connection;

    public void setConnection(Connection c){
        this.connection = c;
    }

    public Connection getConnection() {
        return connection;
    }

    private String scriptPath;

    public String getScriptPath() {
        if (scriptPath==null || scriptPath.isBlank()){
            ClassLoader loader = Database.class.getClassLoader();
            URL url = loader.getResource("data");
            scriptPath = url.getPath();
        }
        return scriptPath;
    }

    public boolean isTableExists(String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, tableName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()){
            String name = rs.getString(1);
           return tableName.equalsIgnoreCase(name);
        }
        return false;
    }

    public int getVersion() {
        String sql = "select ver from migration order by update_at limit 1";

        try {
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException throwables) {
            return 0;
        }

        return 0;
    }

    protected String[] findScripts(){
        File dir = new File(this.getScriptPath());
        String[] files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".sql");
            }
        });
        return files;
    }

    public void migrate() throws SQLException {
        String dataPath = this.getScriptPath();
        String[] scripts = this.findScripts();
        Arrays.sort(scripts, new ScriptComarator());
        int currentVer = this.getVersion();
        for (String script: scripts){
            int ver = Integer.parseInt(script.substring(0,script.indexOf("_")));
            if (ver > currentVer){
                this.runScript(dataPath + File.separator + script);
                String sql = "insert into migration(ver,update_at) values(?,?)";
                PreparedStatement stmt = this.getConnection().prepareStatement(sql);
                stmt.setLong(1, ver);
                stmt.setLong(2, System.currentTimeMillis());
                stmt.execute();
                stmt.close();
            }
        }
    }

    public void runScript(String path)  {
        String delimiter = ";";
        Scanner scanner;
        try {
            scanner = new Scanner(new File(path)).useDelimiter(";");
        } catch (FileNotFoundException e) {
            logger.atSevere().withCause(e).log("%s not found", path);
            return;
        }
        Statement stmt;
        while(scanner.hasNext()){
            String sql = scanner.next() + delimiter;
            try {
                stmt = this.getConnection().createStatement();
                stmt.execute(sql);
                stmt.close();
            } catch (SQLException throwables) {
                logger.atSevere().withCause(throwables).log("Run SQL script error");
                return;
            }

        }

    }

    static class ScriptComarator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            Integer i1 =  Integer.parseInt(o1.substring(0,o1.indexOf("_")));
            Integer i2 = Integer.parseInt(o2.substring(0,o2.indexOf("_")));
            return i1 - i2;
        }
    }
}
