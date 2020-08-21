package cn.kubeease.dth.cfg;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Reader {

    private Yaml yaml;
    private Configuration config;
    public void read(String path) throws FileNotFoundException {
        this.yaml = new Yaml(new Constructor(Configuration.class));
        InputStream in;
        if (path.isEmpty()) {
            ClassLoader loader = Reader.class.getClassLoader();
            in = loader.getResourceAsStream("config.yaml");
        } else {
            in = new FileInputStream(path);
        }
        config = yaml.load(in);
    }
    public Configuration getConfig(){
        return this.config;
    }


}
