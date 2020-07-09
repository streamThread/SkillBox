public class Main {

  private static final String ROOT_PATH = "hdfs://2b7ad65a0249:8020";

  public static void main(String[] args) throws Exception {

    FileAccess fileAccess = new FileAccess(ROOT_PATH);

    fileAccess.delete("/result");
//    fileAccess.create("/test/");
//    fileAccess.copyFileToHDFS(
//        Objects.requireNonNull(Main.class.getClassLoader().getResource("voina"
//            + "-i-mir.txt")).getPath(), "/test/voina-i-mir.txt");
  }
}
