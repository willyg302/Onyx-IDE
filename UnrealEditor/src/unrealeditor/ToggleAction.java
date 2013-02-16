package unrealeditor;

import javax.swing.*;

public abstract class ToggleAction extends AbstractAction {
    private JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel() {};
    public ToggleAction() {}
    public ToggleAction(String name) {super(name);}
    public boolean isSelected() {return model.isSelected();}
    public void setSelected(boolean selected) {model.setSelected(selected);}
    public JToggleButton.ToggleButtonModel getModel() {return model;}

    public JCheckBoxMenuItem createJCheckBoxMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
        item.setModel(model);
        return item;
    }

    public JRadioButtonMenuItem createJRadioButtonMenuItem() {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(this);
        item.setModel(model);
        return item;
    }
}