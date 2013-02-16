package unrealeditor.searchable;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Handles the side bar search box.
 */
public class SearchBoxController implements KeyListener, ChangeListener, FocusListener {

    //key listeners for the tree
    private KeyListener[] listeners;
    private int caretposition = 0;
    private TreeModelSearcher searcher;
    private StringBuffer searchString = new StringBuffer("");
    private SearchBoxUI searchBoxUI;
    private JTree tree;
    boolean selectAllMatches = false;
    private int currentMatchIndex = -1;

    public SearchBoxController(final JTree tree) {
        this.tree = tree;
        searchBoxUI = new SearchBoxUI(tree);
        searcher = new TreeModelSearcher(tree);
        searcher.addChangeListener(this);
        tree.addKeyListener(this);
        tree.addFocusListener(this);
    }

    public boolean isSelectingAllMatches() {
        return selectAllMatches;
    }

    public void setSelectAllMatches(boolean selectAllMatches) {
        this.selectAllMatches = selectAllMatches;
        stateChanged(null);
    }

    public void keyPressed(final KeyEvent ke) {

        if (searchBoxUI.isUIVisible() || ke.getKeyCode() > 44) {
            ke.consume();
            searchBoxUI.drawCursor(true);
            switch (ke.getKeyCode()) {
                case KeyEvent.VK_BACK_SPACE:
                    if (caretposition > 0) {
                        searchString.deleteCharAt(caretposition - 1);
                        searcher.setSearchString(searchString.toString());
                        caretposition--;
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    stopSearchBehavior();
                    break;
                case KeyEvent.VK_ENTER:
                    stopSearchBehavior();
                    if (selectAllMatches) {
                        selectMatches();
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    searchString.insert(caretposition, " ");
                    caretposition++;
                    searcher.setSearchString(searchString.toString());
                    break;
                case KeyEvent.VK_END:
                    caretposition = searchString.length();
                    break;
                case KeyEvent.VK_HOME:
                    caretposition = 0;
                    break;
                case KeyEvent.VK_LEFT:
                    if (caretposition > 0) {
                        caretposition--;
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (searchString.length() > 0) {
                        if (currentMatchIndex == -1) {
                            //currentMatchIndex = 0;
                        } else if (currentMatchIndex > 0) {
                            currentMatchIndex--;
                        }

                        searchBoxUI.setCurrentMatchIndex(currentMatchIndex);
                        selectCurrentMatch();
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (caretposition < searchString.length()) {
                        caretposition++;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (searchString.length() > 0) {
                        if (currentMatchIndex == -1) {
                            //currentMatchIndex = 0;
                        } else if (currentMatchIndex < searcher.getMatches().size() - 1) {
                            currentMatchIndex++;
                        }

                        searchBoxUI.setCurrentMatchIndex(currentMatchIndex);
                        selectCurrentMatch();
                    }
                    break;
                case KeyEvent.VK_DELETE:
                    if (caretposition < searchString.length()) {
                        searchString.deleteCharAt(caretposition);
                        searcher.setSearchString(searchString.toString());
                    }
                    break;
                    
                case KeyEvent.VK_META: break; // Mac command key do nothing.

                default:
                    // Any printable character
                    if (ke.getKeyCode() > 44) {
                        if (!searchBoxUI.isUIVisible())
                            startSearchBehavior();

                        searchString.insert(caretposition, ke.getKeyChar());
                        caretposition++;
                        searcher.setSearchString(searchString.toString());
                    }
            }

            searchBoxUI.setSearchString(searchString.toString());
            searchBoxUI.setCaretPosition(caretposition);
            searchBoxUI.repaint();
        }
    }

    public void keyReleased(final KeyEvent arg0) {}
    public void keyTyped(final KeyEvent ke) {}

    private void selectMatches() {
        final List<DefaultMutableTreeNode> l = searcher.getMatches();
        if (l.size() > 0 && searchString.length() > 0) {
            final TreePath[] tpath = new TreePath[l.size()];
            for (int i = 0; i < l.size(); i++) {
                tpath[i] = new TreePath(l.get(i).getPath());
            }
            tree.setSelectionPaths(tpath);
            tree.scrollPathToVisible(tpath[tpath.length - 1]);

        } else {
            tree.clearSelection();
        }

    }

    private void selectCurrentMatch() {
        final List<DefaultMutableTreeNode> l = searcher.getMatches();
        if (l.size() == 0 || currentMatchIndex == -1)
            return;
        TreePath tpath = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(l.get(currentMatchIndex)));
        tree.setSelectionPath(tpath);
        tree.scrollPathToVisible(tpath);
    }

    private void startSearchBehavior() {
        searchBoxUI.setUIVisible(true);
        listeners = tree.getKeyListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] != this)
                tree.removeKeyListener(listeners[i]);
        }
    }

    public void stateChanged(final ChangeEvent e) {
        if (searchString.length() > 0) {
            int matchCount = searcher.getMatches().size();
            searchBoxUI.setMatchCount(matchCount);
            if (matchCount > 0) {
                if (selectAllMatches) {
                    currentMatchIndex = -1;
                    selectMatches();
                } else {
                    currentMatchIndex = 0;
                    selectCurrentMatch();
                }
            } else {
                currentMatchIndex = -1;
            }
            searchBoxUI.setCurrentMatchIndex(currentMatchIndex);
        } else {
            selectMatches();
        }
    }

    private void stopSearchBehavior() {
        searchBoxUI.setUIVisible(false);
        searchString.setLength(0);
        caretposition = 0;
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] != this) {
                tree.addKeyListener(listeners[i]);
            }
        }

    }

    public SearchBoxUI getSearchBoxUI() {
        return searchBoxUI;
    }

    public void focusGained(FocusEvent arg0) {
        searchBoxUI.showCursor();
    }

    public void focusLost(FocusEvent arg0) {
        searchBoxUI.hideCursor();
    }

    public void requestFocus() {
        tree.requestFocus();
    }

    public JTree getTree() {
        return tree;
    }
}