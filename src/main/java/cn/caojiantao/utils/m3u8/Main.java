package cn.caojiantao.utils.m3u8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author caojiantao
 */
public class Main {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        try (InputStream is = new FileInputStream("/Users/caojiantao/Desktop/test.m3u8")) {
            M3U8Utils.parseM3U8(is, "http://data.video.iqiyi.com/videos/vts/20190923/16/01/", "盗梦空间", "/Users/caojiantao", executorService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
