package cn.kubeease.dth.utils;

import com.google.common.flogger.FluentLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

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

    public Path getScriptPath() throws IOException, URISyntaxException {

        URI uri = Database.class.getResource("/data").toURI();
        Path scriptPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
            }
//            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            scriptPath = fileSystem.getPath("/data");
        } else {
            scriptPath = Paths.get(uri);
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

    protected Path[] findScripts() throws URISyntaxException, IOException {
//        File dir = new File(this.getScriptPath());
//        String[] files = dir.list(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                System.out.println(dir+name);
//                return name.endsWith(".sql");
//            }
//        });
        Path scriptPath = this.getScriptPath();
        Stream<Path> walk = Files.walk(scriptPath, 1);
        List<Path> files = new ArrayList<>();

        for (Iterator<Path> it = walk.iterator(); it.hasNext();){
            Path p = it.next();
            if (p != scriptPath)
                files.add(p);
        }
        return files.toArray(new Path[0]);
    }

    public void migrate() throws SQLException, IOException, URISyntaxException {
        Path dataPath = this.getScriptPath();
        Path[] scripts = this.findScripts();
        Arrays.sort(scripts, new ScriptComarator());
        int currentVer = this.getVersion();
        for (Path script: scripts){
            String fileName = script.getFileName().toString();
            int ver = Integer.parseInt(fileName.substring(0,fileName.indexOf("_")));
            if (ver > currentVer){
                this.runScript(script.toString());
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

    static class ScriptComarator implements Comparator<Path> {
        @Override
        public int compare(Path p1, Path p2) {
            String o1 = p1.getFileName().toString();
            String o2 = p2.getFileName().toString();
            Integer i1 =  Integer.parseInt(o1.substring(0,o1.indexOf("_")));
            Integer i2 = Integer.parseInt(o2.substring(0,o2.indexOf("_")));
            return i1 - i2;
        }
    }
}
