public class Main {

  private static final String ROOT_PATH = "hdfs://f8502fd9fbed:8020";

  public static void main(String[] args) throws Exception {

    FileAccess fileAccess = new FileAccess(ROOT_PATH);

    fileAccess.create("/test/");
    fileAccess.create("/test/test.txt");
    fileAccess.append("/test/test.txt", "Hello Hadoop from JavaCode");
    System.out.println(fileAccess.read("/test/test.txt"));
    System.out.println("Directory \"test/\" is exists: " + fileAccess.isDirectory("/test/"));
    System.out.println("List of paths in \"test/\": " +
        String.join(", ", fileAccess.list("/test/")));
    System.out.println("Deleting \"test/\"");
    fileAccess.delete("/test/");
    System.out.println("Directory \"test/\" is exists: " + fileAccess.isDirectory("/test/"));
  }
}
