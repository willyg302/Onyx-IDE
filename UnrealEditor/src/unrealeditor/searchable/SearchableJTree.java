package unrealeditor.searchable;

import javax.swing.JTree;

/*
 * Class to make any JTree searchable. 
 */
public final class SearchableJTree {
	
	/*
	 * Installs a search UI on a layer above the JTree which
	 * becomes visible when the user types a printable character
	 * when the JTree is in focus. Tree nodes that match the search
	 * are selected in the JTree. Pressing Escape or Enter hides the
	 * search UI.
	 */
	public static SearchBoxController makeSearchable(final JTree tree) {
		return new SearchBoxController(tree);
	}
}
