package com.pius.im.tcp;

import com.pius.im.codec.config.BootstrapConfig;
import com.pius.im.tcp.server.ImServer;
import com.pius.im.tcp.server.ImWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Slf4j
public class Starter {

    public static void main(String[] args) {
        if (args.length > 0) {
            start(args[0]);
        }
    }

    private static void start(String path) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new ImServer(bootstrapConfig.getIm()).start();
            new ImWebSocketServer(bootstrapConfig.getIm()).start();
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            System.exit(500);
        }
    }
}
