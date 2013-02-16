package unrealeditor.searchable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import javax.swing.JTree;


/**
 * Supports searching of a TreeModel for particular search term(s).
 * 
 * This class uses the glazedlists codebase to actually perform the search.
 * Visit GlazedLists at http://publicobject.com/glazedlists/
 */
public class TreeModelSearcher extends TextMatcherEditor implements
        MatcherEditor.Listener, TreeModelListener {

    /** List of all the nodes in the tree **/
    private List<DefaultMutableTreeNode> allNodes;
    /** the matcher determines whether elements get filtered in or out */
    private Matcher currentMatcher = Matchers.trueMatcher();
    /** the list of search results **/
    private List<DefaultMutableTreeNode> currentMatches = new ArrayList<DefaultMutableTreeNode>();
    /** results change listeners **/
    private EventListenerList listeners = new EventListenerList();
    private String searchString = null;
    
    private JTree tree;

    public TreeModelSearcher(final JTree tree) {
        super(new TextFilterator() {

            public void getFilterStrings(final List baseList, final Object element) {
                baseList.add(element);
            }
        });

        this.tree = tree;
        loadModel(tree.getModel());

        this.addMatcherEditorListener(this);


        //TODO  model.addTreeModelListener(this);

    }

    public void addChangeListener(final ChangeListener listener) {
        listeners.add(ChangeListener.class, listener);
    }

    /**
     * Handles changes to the behavior of the filter. This may change the
     * contents of this {@link EventList} as elements are filtered and
     * unfiltered.
     */
    private void changed() {
        currentMatches.clear();
        for (int i = 0; i < allNodes.size(); i++) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.get(i);
            if (currentMatcher.matches(node.toString())) {
                currentMatches.add(node);
            }
        }
    }

    public void changedMatcher(final Event matcherEvent) {
        final MatcherEditor matcherEditor = matcherEvent.getMatcherEditor();
        final Matcher matcher = matcherEvent.getMatcher();
        final int changeType = matcherEvent.getType();

        changeMatcher(matcherEditor, matcher, changeType);

    }

    /**
     * This method selects an appropriate delegate method to perform the correct
     * work for each of the possible <code>changeType</code>s. This method
     * does <strong>NOT</strong> acquire any locks and is thus used during
     * initialization of FilterList.
     */
    private void changeMatcher(final MatcherEditor matcherEditor, final Matcher matcher,
            final int changeType) {
        switch (changeType) {
            case MatcherEditor.Event.CONSTRAINED:
                currentMatcher = matcher;
                this.constrained();
                break;
            case MatcherEditor.Event.RELAXED:
                currentMatcher = matcher;
                this.relaxed();
                break;
            case MatcherEditor.Event.CHANGED:
                currentMatcher = matcher;
                this.changed();
                break;
            case MatcherEditor.Event.MATCH_ALL:
                currentMatcher = Matchers.trueMatcher();
                this.matchAll();
                break;
            case MatcherEditor.Event.MATCH_NONE:
                currentMatcher = Matchers.falseMatcher();
                this.matchNone();
                break;
        }
    }

    /**
     * Handles a constraining or narrowing of the filter. This may change the
     * contents of this {@link EventList} as elements are further filtered due
     * to the constraining of the filter.
     */
    private void constrained() {
        for (int i = currentMatches.size() - 1; i > -1; i--) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentMatches.get(i);
            if(!currentMatcher.matches(node.toString())) {

            //if (!currentMatcher.matches(((Element)((DOMTreeFull)tree).getNode(node)).getTagName())) {
                currentMatches.remove(node);
            }
        }
    }

    void fireChangeEvent() {
        final Object[] l = listeners.getListenerList();
        for (int i = 0; i < l.length; i += 2) {
            if (l[i] == ChangeListener.class) {
                ((ChangeListener) l[i + 1]).stateChanged(null);
            }
        }
    }

    /**
     * Must call {@link #setSearchString(String) setSearchString} before calling this method. 
     * @return List of DefaultMutableTreeNodes that match the search string
     */
    public List<DefaultMutableTreeNode> getMatches() {
        return currentMatches;
    }

    private void loadModel(final TreeModel model) {
        allNodes = new ArrayList();
        final Enumeration e = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
        while (e.hasMoreElements())
            allNodes.add((DefaultMutableTreeNode) e.nextElement());
        currentMatches.addAll(allNodes);
    }

    /**
     * Handles a clearing of the filter. That is, the filter list will act as a
     * passthrough and not discriminate any of the elements of the wrapped
     * source list.
     */
    private void matchAll() {
        currentMatches.clear();
        currentMatches.addAll(allNodes);
    }

    /**
     * Handles a constraining of the filter to a degree that guarantees no
     * values can be matched. That is, the filter list will act as a total
     * filter and not match any of the elements of the wrapped source list.
     */
    private void matchNone() {
        currentMatches.clear();
    }

    private void refilter() {
        final String text = searchString;
        String[] filters = null;

        filters = text.split("[ \t]");

        setFilterText(filters);

        fireChangeEvent();
    }

    /**
     * Handles a relaxing or widening of the filter. This may change the
     * contents of this {@link EventList} as filtered elements are unfiltered
     * due to the relaxation of the filter.
     */
    private void relaxed() {
        for (int i = 0; i < allNodes.size(); i++) {
            final Object node = allNodes.get(i);

            if (!currentMatches.contains(node)
                    && currentMatcher.matches(((DefaultMutableTreeNode) node).toString())) {
                currentMatches.add((DefaultMutableTreeNode) node);
            }
        }
    }

    public void removeChangeListener(final ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
    }

    /**
     * Searches the TreeModel for the given string. 
     * Call {@link #getMatches() getMatches()} to get the results 
     */
    public void setSearchString(final String searchStr) {
        searchString = searchStr;
        refilter();
    }

    /**
     * Utility method that calls {@link #setSearchString(String) setSearchString}
     * followed by {@link #getMatches() getMatches()} to returns the list of DefaultMutableTreeNodes
     * that match the given search string.
     *  
     * @param searchStr The search string
     * @return list of DefaultMutableTreeNodes that match the search string
     */
    public List<DefaultMutableTreeNode> search(final String searchStr) {
        setSearchString(searchStr);
        return getMatches();
    }

    public void treeNodesChanged(final TreeModelEvent arg0) {}
    public void treeNodesInserted(final TreeModelEvent e) {}
    public void treeNodesRemoved(final TreeModelEvent e) {}
    public void treeStructureChanged(final TreeModelEvent arg0) {}
}
