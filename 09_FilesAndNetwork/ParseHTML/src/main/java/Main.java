import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private final static String URL = "https://lenta.ru/";
    private final static String PATH_TO_SAVE_IMAGE = "data";
    private final static String URL_REGEX = "^https?:\\/\\/.+\\..+\\/?$";

    public static void main(String[] args) {
        try {
            parseImagesFromHTML(URL, PATH_TO_SAVE_IMAGE);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static void parseImagesFromHTML(String url, String localPathToSaveImages) throws IOException, IllegalArgumentException {
        if (!url.matches(URL_REGEX)) {
            throw new IllegalArgumentException("Wrong url argument");
        }
        if (!Files.isDirectory(Path.of(localPathToSaveImages))) {
            throw new IllegalArgumentException("Wrong localPathToSaveImages argument");
        }
        Document document = Jsoup.connect(url).get();
        Elements images = document.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
        if (images == null) {
            throw new IOException("No Images found");
        }
        for (Element image : images) {
            String imageUrl = image.attr("src");
            int indexName = imageUrl.lastIndexOf("/") + 1;
            String name = imageUrl.substring(indexName);
            System.out.println("Downloading image: " + name);
            FileOutputStream out = new FileOutputStream(localPathToSaveImages + File.separator + name);
            out.write(new URL(imageUrl).openStream().readAllBytes());
            out.close();
        }
    }
}

