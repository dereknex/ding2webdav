package cn.kubeease.dth.cfg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class ReaderTests {
    private final Reader reader = new Reader();
    @Test
    void TestRead() throws FileNotFoundException {
       reader.read("");
       Configuration cfg = reader.getConfig();
        Assertions.assertEquals(cfg.getCorpID(), "ding7779cf9da65ca5ea");
    }
}
