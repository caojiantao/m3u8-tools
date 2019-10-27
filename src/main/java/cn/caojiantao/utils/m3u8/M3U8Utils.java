package cn.caojiantao.utils.m3u8;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @author caojiantao
 */
public class M3U8Utils {

    private static DecimalFormat decimalFormat = new DecimalFormat("0000");

    /**
     * 解析 m3u8 分片数据至本地，并保存新的 index 索引文件
     *
     * @param indexInputStream m3u8 索引文件输入流
     * @param contextPath      ts 分片上下文路径
     * @param name             电影名
     * @param parent           目标文件夹
     * @param executorService  任务线程池
     */
    public static void parseM3U8(InputStream indexInputStream, String contextPath, String name, String parent, ExecutorService executorService) {
        System.out.println("开始解析 m3u8 视频...");
        long time = System.currentTimeMillis();
        try {
            URL url = new URL(contextPath);
            // 目标文件夹
            File targetDir = new File(parent, name);
            if (!FileUtils.newFile(targetDir, true)) {
                System.out.println("创建目标文件夹失败：" + targetDir.getAbsoluteFile());
                return;
            }
            // 新索引文件
            File indexFile = new File(targetDir, name + ".m3u8");
            if (!FileUtils.newFile(indexFile, false)) {
                System.out.println("创建索引文件失败：" + indexFile.getAbsoluteFile());
                return;
            }
            try (BufferedReader reader = new BufferedReader(new BufferedReader(new InputStreamReader(indexInputStream)));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile, true))) {
                String line;
                int tsIndex = 0;
                List<ParseTsTask> taskList = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().equals("") && !line.startsWith("#")) {
                        // 分片地址处理
                        if (!line.startsWith("http")) {
                            if (line.startsWith("/")) {
                                String rootPath = url.getProtocol() + "://" + url.getHost();
                                line = rootPath + line;
                            } else {
                                line = contextPath + line;
                            }
                        }
                        // 构造分片任务
                        String tsIndexFormat = decimalFormat.format(tsIndex);
                        File ts = new File(targetDir, name + "." + tsIndexFormat + ".ts");
                        if (FileUtils.newFile(ts, false)) {
                            taskList.add(new ParseTsTask(line, ts));
                        } else {
                            System.out.println("创建分片失败：" + ts.getAbsoluteFile());
                        }
                        line = ts.getName();
                        tsIndex++;
                    }
                    // 追加内容至新索引文件
                    writer.write(line);
                    writer.newLine();
                }
                // 解析分片任务
                System.out.println("开始解析分片任务，大小为：" + taskList.size());
                if (executorService == null) {
                    // 单线程
                    taskList.forEach(ParseTsTask::run);
                } else {
                    // 多线程
                    CountDownLatch latch = new CountDownLatch(taskList.size());
                    taskList.forEach(item -> {
                        item.setLatch(latch);
                        executorService.submit(item);
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        executorService.shutdown();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("解析完成，耗时 " + (System.currentTimeMillis() - time) + " ms");
    }
}
