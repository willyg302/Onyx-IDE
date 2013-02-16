package unrealeditor;

import javax.swing.JLabel;
import java.util.Stack;

/**
 * Allows texts to be pushed and popped. Pushed texts have precedence over set texts.
 * Good for status bars wanting to display temporary messages for instance.
 */
public class JStackLabel extends JLabel {
    private String backgroundText;
    private Stack stack = new Stack();

    public JStackLabel() {}

    public void pushText(String text) {
        stack.push(text);
        super.setText(text);
    }

    public void popText(String text) {
        stack.remove(text);
        if(stack.empty()) super.setText(backgroundText);
        else super.setText((String)stack.peek());
    }

    public void setText(String text) {
        this.backgroundText = text;
        if(stack != null && stack.empty()) super.setText(text);
    }
}