package com.pius.im.common.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Slf4j
public class SignAPI {

    final private long appId;
    final private String key;

    public SignAPI(long appId, String key) {
        this.appId = appId;
        this.key = key;
    }

    public static void main(String[] args) {
        SignAPI signAPI = new SignAPI(10000, "123456");
        String sign = signAPI.genUserSign("zhangsan", 200000);
//        Thread.sleep(2000L);

        JSONObject jsonObject = decodeUserSign(sign);
        System.out.println("sign:" + sign);
        System.out.println("decoder:" + jsonObject.toString());
    }

    /**
     * 解密方法
     */
    public static JSONObject decodeUserSign(String userSign) {
        JSONObject sigDoc = new JSONObject(true);
        byte[] decodeUrlByte = Base64URL.base64DecodeUrl(userSign.getBytes());
        byte[] decompressByte = decompress(decodeUrlByte);
        String decodeText = new String(decompressByte, StandardCharsets.UTF_8);

        if (StringUtils.isNotBlank(decodeText)) {
            sigDoc = JSONObject.parseObject(decodeText);
        }

        return sigDoc;
    }

    /**
     * 解压缩
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!inflater.finished()) {
                int i = inflater.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            log.error(Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }
        }

        inflater.end();

        return output;
    }

    public String genUserSign(String identifier, long expire) {
        return genUserSign(identifier, expire, null);
    }

    private String genUserSign(String identifier, long expire, byte[] userBuf) {

        long expireTime = System.currentTimeMillis() / 1000 + expire / 1000;

        JSONObject signDoc = new JSONObject();
        signDoc.put("TLS.appId", appId);
        signDoc.put("TLS.identifier", identifier);
        signDoc.put("TLS.expire", expire);
        signDoc.put("TLS.expireTime", expireTime);

        String base64UserBuf = null;
        if (userBuf != null) {
            base64UserBuf = Base64.getEncoder().encodeToString(userBuf).replaceAll("\\s*", "");
            signDoc.put("TLS.userbuf", base64UserBuf);
        }
        String sign = hmacsha256(identifier, expire, expireTime, base64UserBuf);
        if (sign.isEmpty()) {
            return "";
        }
        signDoc.put("TLS.sign", sign);
        Deflater compressor = new Deflater();
        compressor.setInput(signDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return (new String(Base64URL.base64EncodeUrl(Arrays.copyOfRange(compressedBytes,
                0, compressedBytesLength)))).replaceAll("\\s*", "");
    }

    public String hmacsha256(String identifier, long expire, long expireTime, String base64UserBuf) {
        String contentToBeSigned = "TLS.appId:" + appId + "\n"
                + "TLS.identifier:" + identifier + "\n"
                + "TLS.expire:" + expire + "\n"
                + "TLS.expireTime:" + expireTime + "\n";
        if (null != base64UserBuf) {
            contentToBeSigned += "TLS.userbuf:" + base64UserBuf + "\n";
        }
        try {
            byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSign = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return (Base64.getEncoder().encodeToString(byteSign)).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return "";
        }
    }

}
