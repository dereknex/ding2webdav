package cn.kubeease.dth.utils;

import com.google.common.flogger.FluentLogger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class Database {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Connection connection;

    public void setConnection(Connection c) {
        this.connection = c;
    }

    public Connection getConnection() {
        return connection;
    }

//    public boolean isTableExists(String tableName) throws SQLException {
//        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
//        PreparedStatement stmt = this.connection.prepareStatement(sql);
//        stmt.setString(1, tableName);
//        ResultSet rs = stmt.executeQuery();
//        if (rs.next()) {
//            String name = rs.getString(1);
//            return tableName.equalsIgnoreCase(name);
//        }
//        return false;
//    }

    public int getVersion() {
        String sql = "select ver from migration order by update_at desc limit 1";

        try {
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return 0;
        }

        return 0;
    }

    protected String[] findScripts()  {
        Reflections reflections = new Reflections("data/", new ResourcesScanner());
        Set<String> fileNames = reflections.getResources(Pattern.compile(".*\\.sql"));
        return fileNames.toArray(new String[0]);
    }

    public void migrate() throws SQLException {
        String[] scripts = this.findScripts();
        assert scripts!=null;
        Arrays.sort(scripts, new ScriptComparator());
        int currentVer = this.getVersion();
        for (String script : scripts) {
            int ver = Integer.parseInt(script.substring(script.lastIndexOf("/")+1, script.indexOf("_")));
            if (ver > currentVer) {
                this.runScript(script);
                String sql = "insert into migration(ver,update_at) values(?,?)";
                PreparedStatement stmt = this.getConnection().prepareStatement(sql);
                stmt.setLong(1, ver);
                stmt.setLong(2, System.currentTimeMillis());
                stmt.execute();
                stmt.close();
            }
        }
    }

    public void runScript(String file) {
        String delimiter = ";";
        Scanner scanner;
        InputStream is = Database.class.getClassLoader().getResourceAsStream(file);
        assert is != null;
        scanner = new Scanner(is).useDelimiter(";");
        Statement stmt;
        while (scanner.hasNext()) {
            String sql = scanner.next() + delimiter;
            try {
                stmt = this.getConnection().createStatement();
                stmt.execute(sql);
                stmt.close();
            } catch (SQLException e) {
                logger.atSevere().withCause(e).log("Run SQL script error");
                return;
            }

        }
        scanner.close();
        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static class ScriptComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            System.out.println(o1+"  "+ o2);
            Integer i1 = Integer.parseInt(o1.substring(o1.lastIndexOf("/")+1, o1.indexOf("_")));
            Integer i2 = Integer.parseInt(o2.substring(o2.lastIndexOf("/")+1, o2.indexOf("_")));
            return i1 - i2;
        }
    }
}
