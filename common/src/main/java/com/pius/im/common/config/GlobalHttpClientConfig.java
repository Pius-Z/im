package com.pius.im.common.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Configuration
@ConfigurationProperties(prefix = "httpclient")
public class GlobalHttpClientConfig {
    @Getter
    @Setter
    private Integer maxTotal; // 最大连接数

    @Getter
    @Setter
    private Integer defaultMaxPerRoute; // 最大并发链接数

    @Getter
    @Setter
    private Integer connectTimeout; // 创建链接的最大时间

    @Getter
    @Setter
    private Integer connectionRequestTimeout; // 链接获取超时时间

    @Getter
    @Setter
    private Integer socketTimeout; // 数据传输最长时间

    @Getter
    @Setter
    private boolean staleConnectionCheckEnabled; // 提交时检查链接是否可用

    PoolingHttpClientConnectionManager manager = null;
    HttpClientBuilder httpClientBuilder = null;

    /**
     * 定义httpClient链接池
     */
    @Bean(name = "httpClientConnectionManager")
    public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
        return getManager();
    }

    private PoolingHttpClientConnectionManager getManager() {
        if (manager != null) {
            return manager;
        }
        manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(maxTotal); // 设定最大链接数
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute); // 设定并发链接数
        return manager;
    }

    /**
     * 实例化连接池，设置连接池管理器。 这里需要以参数形式注入上面实例化的连接池管理器
     * Qualifier 指定bean标签进行注入
     */
    @Bean(name = "httpClientBuilder")
    public HttpClientBuilder getHttpClientBuilder(
            @Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager httpClientConnectionManager) {

        // HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder
        // 可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
        httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(httpClientConnectionManager);
        return httpClientBuilder;
    }

    /**
     * 注入连接池，用于获取httpClient
     */
    @Bean
    public CloseableHttpClient getCloseableHttpClient(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }

    public CloseableHttpClient getCloseableHttpClient() {
        if (httpClientBuilder != null) {
            return httpClientBuilder.build();
        }
        httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(getManager());
        return httpClientBuilder.build();
    }

    /**
     * Builder是RequestConfig的一个内部类 通过RequestConfig的custom方法来获取到一个Builder对象
     * 设置builder的连接信息
     */
    @Bean(name = "builder")
    public RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout).setStaleConnectionCheckEnabled(staleConnectionCheckEnabled);
    }

    /**
     * 使用builder构建一个RequestConfig对象
     */
    @Bean
    public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder) {
        return builder.build();
    }

}
