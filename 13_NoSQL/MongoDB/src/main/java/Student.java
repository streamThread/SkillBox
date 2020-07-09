import com.opencsv.bean.CsvBindAndSplitByPosition;
import com.opencsv.bean.CsvBindByPosition;
import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class Student implements Serializable {

  @CsvBindByPosition(position = 0)
  private String name;
  @CsvBindByPosition(position = 1)
  private Byte age;
  @CsvBindAndSplitByPosition(position = 2, splitOn = "\\,", elementType = String.class)
  private Set<String> courses;
}
