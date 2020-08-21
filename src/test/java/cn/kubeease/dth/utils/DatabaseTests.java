package cn.kubeease.dth.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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
    public void testFindScripts() {
        String[] scripts = db.findScripts();
        Assertions.assertEquals(scripts[0], "001_initial.sql");
    }

    @Test
    void testScriptComarator() {
        String[] scripts = { "001_initial.sql", "003_a.sql", "002_b.sql" };
        Arrays.sort(scripts, new Database.ScriptComarator());
        Assertions.assertEquals(scripts[0], "001_initial.sql");
        Assertions.assertEquals(scripts[1], "002_b.sql");
        Assertions.assertEquals(scripts[2], "003_a.sql");
    }

    @Test
    void testRunScript() {
        String path = db.getScriptPath();
        String filePath = path + File.separator + "001_initial.sql";
        db.runScript(filePath);
    }

    @Test
    void testGetVersion() throws SQLException {
        String path = db.getScriptPath();
        String filePath = path + File.separator + "001_initial.sql";
        db.runScript(filePath);
        int ver = db.getVersion();

        Assertions.assertEquals(ver, 0);
    }
}
