package cn.kubeease.dth.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class DatabaseTests {

    private final Database db = new Database();

    @BeforeEach
    public void prepare() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        db.setConnection(connection);
    }

    @Test
    public void testFindScripts() throws IOException, URISyntaxException {
        Path[] scripts = db.findScripts();
        Assertions.assertEquals(scripts[0].getFileName().toString(), "001_initial.sql");
    }

    @Test
    void testScriptComarator() {
        FileSystem fs = FileSystems.getDefault();
        Path[] scripts = { fs.getPath("001_initial.sql"), fs.getPath("003_a.sql"), fs.getPath("002_b.sql") };
        Arrays.sort(scripts, new Database.ScriptComarator());
        Assertions.assertEquals(scripts[0].getFileName().toString(), "001_initial.sql");
        Assertions.assertEquals(scripts[1].getFileName().toString(), "002_b.sql");
        Assertions.assertEquals(scripts[2].getFileName().toString(), "003_a.sql");
    }

    @Test
    void testRunScript() throws IOException, URISyntaxException {
        Path path = db.getScriptPath();
        String filePath = path.toString() + File.separator + "001_initial.sql";
        db.runScript(filePath);
    }

    @Test
    void testGetVersion() throws SQLException, IOException, URISyntaxException {
        Path path = db.getScriptPath();
        String filePath = path.toString() + File.separator + "001_initial.sql";
        db.runScript(filePath);
        int ver = db.getVersion();

        Assertions.assertEquals(ver, 0);
    }
}
