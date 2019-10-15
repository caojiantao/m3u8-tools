package cn.caojiantao.utils.m3u8;

import java.io.*;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * @author caojiantao
 */


public class ParseTsTask implements Runnable {

    private String tsUrl;
    private File tsFile;

    private CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public ParseTsTask(String tsUrl, File tsFile) {
        this.tsUrl = tsUrl;
        this.tsFile = tsFile;
    }

    @Override
    public void run() {
        System.out.println("开始解析分片任务：" + tsFile.getName() + ", " + tsUrl);
        try (InputStream inputStream = new URL(tsUrl).openStream();
             OutputStream os = new FileOutputStream(tsFile)) {
            byte[] buffer = new byte[1024 * 1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("分片任务解析成功：" + tsFile.getName() + ", " + tsUrl);
            if (latch != null){
                latch.countDown();
            }
        }
    }
}
