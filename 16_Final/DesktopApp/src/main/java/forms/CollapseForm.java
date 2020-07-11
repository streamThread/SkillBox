package forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import util.MyAppMainFrame;

public class CollapseForm {

  private JPanel mainPanel;
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
              .showMessageDialog(mainPanel,
                  "Пожалуйста введите Фамилию и Имя. \r\n"
                      + "Отчество - по желанию",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
          return;
        }
        ExpandForm expandForm = new ExpandForm();
        expandForm.getFullName()
            .setText(surnameStr + " " + nameStr + " " + thirdNameStr);
        MyAppMainFrame.getInstance().remove(mainPanel);
        MyAppMainFrame.getInstance().add(expandForm.getExpandPanel());
        MyAppMainFrame.getInstance().pack();
        MyAppMainFrame.getInstance().repaint();
        MyAppMainFrame.getInstance().validate();
      }
    });
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public JTextField getSurName() {
    return surName;
  }

  public JTextField getName() {
    return name;
  }

  public JTextField getThirdName() {
    return thirdName;
  }
}
