import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        try {
            Document document = Jsoup.connect("https://lenta.ru/").get();
            Elements images = document.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
            for (Element image : images) {
                String url = image.attr("src");
                int indexName = url.lastIndexOf("/")+1;
                String name = url.substring(indexName);
                System.out.println("Downloading image: " + name);
                new FileOutputStream("data/"+ name).write(new URL(url).openStream().readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
