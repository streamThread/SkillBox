import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;

@Log4j2
public class ImageResizer extends Thread {

    Deque<File> deque;
    String dstFolder;
    long start;
    int newWidth;

    public ImageResizer(Deque<File> deque, String dstFolder, long start, int newWidth) {
        if (newWidth <= 0) {
            throw new IllegalArgumentException("Wrong argument Width");
        }
        this.deque = deque;
        this.dstFolder = dstFolder;
        this.start = start;
        this.newWidth = newWidth;
    }

    @Override
    public void run() {
        try {
            while (true) {

                File file = deque.poll();
                if (file == null){
                    break;
                }

                @NonNull
                BufferedImage image = ImageIO.read(file);

                int newHeight = Math.round((long) (image.getHeight() / (image.getWidth() / newWidth)));

                BufferedImage finalImage;

                if (image.getWidth() / newWidth > 2) {
                    BufferedImage newImage = resizeImageNearestNeighbor(image, newWidth * 2, newHeight * 2);
                    finalImage = resizeImageBicubic(newImage, newWidth, newHeight);
                } else {
                    finalImage = resizeImageNearestNeighbor(image, newWidth, newHeight);
                }

                File newFile = new File(dstFolder + File.separator + file.getName());

                ImageIO.write(finalImage, "jpg", newFile);

                //Следующая часть кода сделана для того, чтобы создать демонстрационную директорию с изначальным качеством сжатия
                //для сравнения результатов
                BufferedImage test = resizeImageNearestNeighbor(image, newWidth, newHeight);
                File testFile = new File(dstFolder + File.separator + "bad_resize_example" + File.separator + file.getName());
                if (!testFile.getParentFile().exists()) {
                    Files.createDirectory(Path.of(testFile.getParent()));
                }
                ImageIO.write(test, "jpg", testFile);
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        System.out.println("Duration: " + (System.currentTimeMillis() - start));
    }

    private BufferedImage resizeImageBicubic(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
//        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    private BufferedImage resizeImageNearestNeighbor(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
//        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
}
