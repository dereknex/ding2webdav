package cn.kubeease.dth.cfg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class ReaderTests {
    private final Reader reader = new Reader();

    @Test
    void TestRead() throws FileNotFoundException {
        ClassLoader loader = Reader.class.getClassLoader();
        reader.read(loader.getResource("test.yaml").getPath());
        Configuration cfg = reader.getConfig();
        Assertions.assertEquals(cfg.getCorpID(), "5292536b5f58bc64cc9e32f36788ba9c");
    }
}
