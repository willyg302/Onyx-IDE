package unrealeditor;

import java.awt.*;
import javax.swing.*;

public class MainToolBar extends JToolBar {
    public ToolButton newButton, openButton, saveButton, cutButton, 
            copyButton, pasteButton, undoButton, redoButton,
            compileButton, playButton;
    private MainTextPad inst;
    
    public MainToolBar() {
        super();
        setFloatable(false);
        setMargin(new Insets(2, 0, 2, 0));
        inst = MainTextPad.getInstance();
        
        newButton = new ToolButton(inst.newAction, "Opens a new document.");
        openButton = new ToolButton(inst.openAction, "Opens an existing document.");
        saveButton = new ToolButton(inst.saveAction, "Saves the active document.");
        cutButton = new ToolButton(inst.cutAction, 
                "Cuts the marked contents out of your document.");
        copyButton = new ToolButton(inst.copyAction, 
                "Copies the marked contents into the clipboard.");
        pasteButton = new ToolButton(inst.pasteAction, 
                "Inserts the contents of the clipboard into your document.");
        undoButton = new ToolButton(inst.undoAction, "Undos the last action.");
        redoButton = new ToolButton(inst.redoAction, "Redos the last action.");
        

        add(newButton);
        add(openButton);
        add(saveButton);
        addSeparator();
        add(cutButton);
        add(copyButton);
        add(pasteButton);
        addSeparator();
        add(undoButton);
        add(redoButton);
        
        if(UnrealEditor.isWindows || UnrealEditor.isDevBuild) {
            addSeparator();
            compileButton = new ToolButton(inst.compileAction, "Compiles using Unreal Frontend.");
            playButton = new ToolButton(inst.playAction, "Launches UDK Game.");
            add(compileButton);
            add(playButton);
        }
    }
    
    public class ToolButton extends JButton {
        public ToolButton(Action action, String tooltip) {
            super(action);
            setMargin(new Insets(4, 4, 4, 4));
            setToolTipText(tooltip);
            setText("");
        }
        public boolean isFocusTraversable() {return false;}
        public void requestFocus() {}
    }
}