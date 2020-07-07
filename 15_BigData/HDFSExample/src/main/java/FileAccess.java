import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileAccess {

    private final FileSystem hdfs;

    /**
     * Initializes the class, using rootPath as "/" directory
     *
     * @param rootPath - the path to the root of HDFS,
     *                 for example, hdfs://localhost:32771
     */
    public FileAccess(String rootPath) throws URISyntaxException, IOException {
        Configuration configuration = new Configuration();
        configuration.set("dfs.client.use.datanode.hostname", "true");
        System.setProperty("HADOOP_USER_NAME", "root");
        hdfs = FileSystem.get(new URI(rootPath), configuration);
    }

    /**
     * Creates empty file or directory
     *
     * @param path
     */
    public void create(String path) throws IOException {
        Path hdfsPath = new Path(path);
        if (path.contains(".")) {
            FSDataOutputStream fsDataOutputStream = hdfs.create(hdfsPath);
            fsDataOutputStream.close();
        } else hdfs.mkdirs(hdfsPath);
    }

    /**
     * Appends content to the file
     *
     * @param path
     * @param content
     */
    public void append(String path, String content) throws IOException {
        Path hdfsPath = new Path(path);
        if (hdfs.isFile(hdfsPath)) {
            try (FSDataOutputStream fsDataOutputStream = hdfs.append(hdfsPath)) {
                fsDataOutputStream.write(content.getBytes());
            }
        } else throw new IllegalArgumentException("Not a file");
    }

    /**
     * Returns content of the file
     *
     * @param path
     * @return
     */
    public String read(String path) throws IOException {
        String result = "";
        Path hdfsPath = new Path(path);
        if (hdfs.isFile(hdfsPath)) {
            try (FSDataInputStream inputStream = hdfs.open(hdfsPath)) {
                result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } else throw new IllegalArgumentException("Not a file");
        return result;
    }

    /**
     * Deletes file or directory
     *
     * @param path
     */
    public void delete(String path) throws IOException {
        hdfs.delete(new Path(path), true);
    }

    /**
     * Checks, is the "path" is directory or file
     *
     * @param path
     * @return
     */
    public boolean isDirectory(String path) throws IOException {
        return hdfs.isDirectory(new Path(path));
    }

    /**
     * Return the list of files and subdirectories on any directory
     *
     * @param path
     * @return
     */
    public List<String> list(String path) throws IOException {
        Path hdfsPath = hdfs.resolvePath(new Path(path));
        Queue<Path> fileQueue = new LinkedList<>();
        List<String> filePaths = new ArrayList<>();
        fileQueue.add(hdfsPath);
        while (!fileQueue.isEmpty()) {
            Path filePath = fileQueue.remove();
            filePaths.add(filePath.toString());
            if (hdfs.isDirectory(filePath)) {
                FileStatus[] fileStatuses = hdfs.listStatus(filePath);
                for (FileStatus fileStatus : fileStatuses) {
                    fileQueue.add(fileStatus.getPath());
                }
            }
        }
        return filePaths;
    }
}
