package unrealeditor;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;

/**
 * DOMTree class to enter every DOM node into a Swing JTree tree.
 * The forward and backward mappings between Nodes to TreeNodes are kept.
 */
public class DOMTreeFull extends JTree {
    private static final long serialVersionUID = 3978144335541975344L;

    /**
     * Reads from an XML InputStream and generates the resulting tree.
     * bNormalize is mainly to distinguish from the other constructor,
     * but it can also be used to non-normalize (I suppose...)
     */
    public DOMTreeFull(InputStream xmlFile, boolean bNormalize) {
        super(new Model());
        Document document = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(xmlFile);
            if(bNormalize)
                document.getDocumentElement().normalize();
        } catch(Exception e) {
            System.out.println("[DOMTreeFull] Error parsing document");
        }
        initDOMTree((Node)document);
    }
    
    public DOMTreeFull() {
        this(null);
    }

    public DOMTreeFull(Node root) {
        super(new Model());
        initDOMTree(root);
    }
    
    public final void initDOMTree(Node root) {
        setRootVisible(false);
        // set properties (IMPORTANT: SKIP DOCUMENT NODE)
        setRootNode(root.getFirstChild());
    }

    public void setRootNode(Node root) {
        ((Model) getModel()).setRootNode(root);
        expandRow(0);
    }

    public Node getRootNode() {
        return ((Model) getModel()).getRootNode();
    }

    /** Get the org.w3c.Node for a MutableTreeNode. */
    public Node getNode(Object treeNode) {
        return ((Model) getModel()).getNode(treeNode);
    }

    /** Get the TreeNode for a org.w3c.Node. */
    public TreeNode getTreeNode(Object node) {
        return ((Model) getModel()).getTreeNode(node);
    }

    /**
     * DOM tree model
     */
    public static class Model extends DefaultTreeModel implements Serializable {
        private static final long serialVersionUID = 3258131375181018673L;
        private Node myRoot;
        private Hashtable nodeMap = new Hashtable();
        private Hashtable treeNodeMap = new Hashtable();

        public Model() {
            this(null);
        }

        public Model(Node node) {
            super(new DefaultMutableTreeNode());
            if (node != null)
                setRootNode(node);
        }

        public synchronized void setRootNode(Node root) {
            this.myRoot = root;
            DefaultMutableTreeNode where = (DefaultMutableTreeNode) getRoot();
            where.removeAllChildren();
            nodeMap.clear();
            treeNodeMap.clear();
            buildTree(root, where);
            fireTreeStructureChanged(this, new Object[]{getRoot()}, new int[0], new Object[0]);
        }

        public Node getRootNode() {
            return myRoot;
        }

        /** Get the org.w3c.Node for a MutableTreeNode. */
        public Node getNode(Object treeNode) {
            return (Node) nodeMap.get(treeNode);
        }

        public Hashtable getAllNodes() {
            return nodeMap;
        }

        /** Get the TreeNode for a org.w3c.Node. */
        public TreeNode getTreeNode(Object node) {
            return (TreeNode) treeNodeMap.get(node);
        }

        private void buildTree(Node node, MutableTreeNode where) {
            if (node == null)
                return;
            MutableTreeNode treeNode = insertNode(node, where);
            // iterate over children of this node
            NodeList nodes = node.getChildNodes();
            int len = (nodes != null) ? nodes.getLength() : 0;
            for (int i = 0; i < len; i++) {
                Node child = nodes.item(i);
                buildTree(child, treeNode);
            }
        }

        /** Inserts a node and returns a reference to the new node. */
        private MutableTreeNode insertNode(String what, MutableTreeNode where) {
            MutableTreeNode node = new DefaultMutableTreeNode(what);
            insertNodeInto(node, where, where.getChildCount());
            return node;
        }

        /** Inserts a text node. */
        public MutableTreeNode insertNode(Node what, MutableTreeNode where) {
            MutableTreeNode treeNode;
            if (what.getNodeType() == Node.ELEMENT_NODE) {
                treeNode = insertNode(DOMTreeFull.toString(what), where);
                nodeMap.put(treeNode, what);
                treeNodeMap.put(what, treeNode);
            } else {
                treeNode = new DefaultMutableTreeNode(what);
            }
            return treeNode;
        }
    }
    
    public static String whatArray[] = new String[]{
        "ALL",
        "ELEMENT",
        "ATTRIBUTE",
        "TEXT",
        "CDATA_SECTION",
        "ENTITY_REFERENCE",
        "ENTITY",
        "PROCESSING_INSTRUCTION",
        "COMMENT",
        "DOCUMENT",
        "DOCUMENT_TYPE",
        "DOCUMENT_FRAGMENT",
        "NOTATION"
    };
    
    public static String toString(Node node) {
        StringBuffer sb = new StringBuffer();
        if (node == null)
            return "";

        int type = node.getNodeType();
        //sb.append(whatArray[type]);
        //sb.append(" : ");
        sb.append(node.getNodeName());
        String value = node.getNodeValue();
        if (value != null) {
            sb.append(" Value: \"");
            sb.append(value);
            sb.append("\"");
        }

        switch (type) {
            case Node.DOCUMENT_NODE: break;

            // element with attributes
            case Node.ELEMENT_NODE:
                /*
                Attr attrs[] = sortAttributes(node.getAttributes());
                if (attrs.length > 0) {
                    sb.append(" ATTRS:");
                }
                for (int i = 0; i < attrs.length; i++) {
                    Attr attr = attrs[i];

                    sb.append(' ');
                    sb.append(attr.getNodeName());
                    sb.append("=\"");
                    sb.append(normalize(attr.getNodeValue()));
                    sb.append('"');
                }
                sb.append('>');*/
                break;
            
            case Node.ENTITY_REFERENCE_NODE: break;
            case Node.CDATA_SECTION_NODE: break;
            case Node.TEXT_NODE: break;
            case Node.PROCESSING_INSTRUCTION_NODE: break;
            case Node.COMMENT_NODE: break;
            case Node.DOCUMENT_TYPE_NODE: break;
            
            // Notation node
            case Node.NOTATION_NODE:
                sb.append("public:");
                String id = ((Notation) node).getPublicId();
                if (id == null) {
                    sb.append("PUBLIC ");
                    sb.append(id);
                    sb.append(" ");
                }
                id = ((Notation) node).getSystemId();
                if (id == null) {
                    sb.append("system: ");
                    sb.append(id);
                    sb.append(" ");
                }
                break;
        }
        return sb.toString();
    }

    /** Normalizes the given string. */
    static protected String normalize(String s) {
        StringBuffer str = new StringBuffer();
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': str.append("&lt;"); break;
                case '>': str.append("&gt;"); break;
                case '&': str.append("&amp;"); break;
                case '"': str.append("&quot;"); break;
                case '\r': case '\n': default: str.append(ch);
            }
        }
        return str.toString();
    }

    /** Returns a sorted list of attributes. */
    static protected Attr[] sortAttributes(NamedNodeMap attrs) {
        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++)
            array[i] = (Attr) attrs.item(i);
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }
        return array;
    }
}