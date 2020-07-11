package forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import util.MyAppMainFrame;

public class ExpandForm {

  private static final String FULL_NAME_LATIN =
      "((?<=\\b)[A-Z][a-z]{0,99}(-[A-Z][a-z]{0,99}){0,20}\\s*){2,3}";
  private static final String FULL_NAME_CYRILLIC =
      "((?<=\\b)[А-Я][а-я]{0,99}(-[А-Я][а-я]{0,99}){0,20}\\s*){2,3}";
  private JPanel inputPanel;
  private JTextField fullName;
  private JButton expandButton;
  private JPanel expandPanel;

  public ExpandForm() {
    expandButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String fullNameStr = fullName.getText().strip();
        if (!fullNameStr.matches(FULL_NAME_LATIN) &&
            !fullNameStr.matches(FULL_NAME_CYRILLIC)) {
          JOptionPane
              .showMessageDialog(expandPanel,
                  "Пожалуйста введите Фамилию и Имя. \r\n"
                      + "Отчество - по желанию",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
          return;
        }

        String[] nameParts = fullNameStr.split("\\s+");
        CollapseForm collapseForm = new CollapseForm();
        collapseForm.getSurName().setText(nameParts[0]);
        collapseForm.getName().setText(nameParts[1]);
        if (nameParts.length == 3) {
          collapseForm.getThirdName().setText(nameParts[2]);
        }
        MyAppMainFrame.getInstance().remove(expandPanel);
        MyAppMainFrame.getInstance().add(collapseForm.getMainPanel());
        MyAppMainFrame.getInstance().pack();
        MyAppMainFrame.getInstance().repaint();
        MyAppMainFrame.getInstance().validate();
      }
    });
  }

  public JPanel getExpandPanel() {
    return expandPanel;
  }

  public JTextField getFullName() {
    return fullName;
  }
}
