package xml_to_csv;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import model.Voter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;

public class CSVCreator {

    private static final Logger logger = LogManager.getRootLogger();
    private final Path filePath;
    private Writer writer;
    private StatefulBeanToCsv<Voter> beanToCsv;

    CSVCreator(Path path) {
        filePath = path;
        refreshDir();
        try {
            writer = new FileWriter(filePath.toFile(), true);
            beanToCsv = new StatefulBeanToCsvBuilder<Voter>(writer).build();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void writeToCsv(Voter voter) {
        try {
            beanToCsv.write(voter);
            writer.flush();
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException ex) {
            logger.error(ex);
        }
    }

    private void refreshDir() {
        try {
            Path dir = filePath.getParent();
            if (Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            Files.createDirectory(dir);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
