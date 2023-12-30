package com.pius.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@SpringBootApplication
@MapperScan("com.pius.message.dao.mapper")
public class MessageStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageStoreApplication.class, args);
    }
}
