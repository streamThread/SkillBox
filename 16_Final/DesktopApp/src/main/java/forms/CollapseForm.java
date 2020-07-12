package forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import util.MyAppMainFrame;

public class CollapseForm implements MyForm {

  private JPanel collapsePanel;
  private JTextField surName;
  private JTextField name;
  private JTextField thirdName;
  private JButton collapseButton;
  private JPanel inputPanel;

  public CollapseForm() {

    collapseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String nameStr = name.getText();
        String surnameStr = surName.getText();
        String thirdNameStr = thirdName.getText();
        if (nameStr.isBlank() || surnameStr.isBlank()) {
          JOptionPane
              .showMessageDialog(collapsePanel,
                  "Пожалуйста введите Фамилию и Имя. \r\n"
                      + "Отчество - по желанию",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
          return;
        }
        ExpandForm expandForm = new ExpandForm(
            String.join(" ", surnameStr, nameStr, thirdNameStr));
        MyAppMainFrame.getInstance().changeMainForm(expandForm);

      }
    });
  }

  public CollapseForm(String... fullNameParts) {
    this();
    int fullNamePartsLenght = fullNameParts.length;
    if (fullNamePartsLenght > 3) {
      throw new IllegalArgumentException(
          "Wrong arguments count " + fullNamePartsLenght);
    }
    if (fullNamePartsLenght >= 1) {
      surName.setText(fullNameParts[0]);
    }
    if (fullNamePartsLenght >= 2) {
      name.setText(fullNameParts[1]);
    }
    if (fullNamePartsLenght == 3) {
      thirdName.setText(fullNameParts[2]);
    }
  }

  @Override
  public JPanel getContentPanel() {
    return collapsePanel;
  }
}
