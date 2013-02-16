package unrealeditor.searchable;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.Timer;

/**
 * Installs and displays the search UI over the JTree. 
 * 
 * The overlay is installed using the AbstractComponentDecorator class
 * written by Timothy Wall (http://sourceforge.net/users/twall/)
 * with one modification to the getDecoratedBounds() method. 
 */
public class SearchBoxUI extends AbstractComponentDecorator {

    //Customizable properties
    private Color background = Color.black;
    private Color foreground = Color.white;
    private float transparency = 0.8f;
    private final int BAND_HEIGHT = 76;
    private boolean visible = false;
    private int matchCount;
    private int currentMatchIndex = -1;
    private String searchString;
    private int caretPosition;
    private boolean drawcursor = false;
    public static int POSITION_TOP = 0;
    public static int POSITION_CENTER = 1;
    public static int POSITION_BOTTOM = 2;
    private int position = POSITION_BOTTOM;
    private Timer cursorBlinkTimer;
    final Action cursorblinker = new AbstractAction() {
        private static final long serialVersionUID = 3159870908626188114L;
        public void actionPerformed(ActionEvent e) {
            repaint();
            drawcursor = !drawcursor;
        }
    };
    private final BasicStroke cursorStroke = new BasicStroke(1);
    //cursor blink rate
    private int delay = 500;
    private final BasicStroke magglassStroke = new BasicStroke(2);
    private final BasicStroke maghandleStroke = new BasicStroke(4,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private final Font mainfont = new Font("Helvetica", Font.BOLD, 16);
    private final Font resultsfont = new Font("Helvetica", Font.BOLD, 10);

    public SearchBoxUI(JTree tree) {

        super(tree);

        //start cursor blinking
        cursorBlinkTimer = new Timer(delay, cursorblinker);
        cursorBlinkTimer.start();
        this.setUIVisible(false);
    }

    @Override
    public void paint(final Graphics graphics) {
        if (!isUIVisible())
            return;

        final Rectangle r = getDecorationBounds();
        final Graphics2D g = (Graphics2D) graphics;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparency));

        //with space for one row from top or bottom
        int rowheight = ((JTree) this.getComponent()).getRowHeight();
        int bandtopy = r.y + rowheight;

        if (position == POSITION_BOTTOM) {
            bandtopy = r.y + r.height - BAND_HEIGHT - rowheight;
        } else if (position == POSITION_CENTER) {
            bandtopy = r.y + (r.height / 2) - (BAND_HEIGHT / 2);
        }
        drawStaticItems(g, bandtopy);

        // draw search string
        g.setFont(mainfont);
        g.setColor(foreground);
        int y = bandtopy + BAND_HEIGHT / 2 - 10;
        g.drawString(searchString, r.x + 10, y);

        // draw cursor
        final FontMetrics fm = g.getFontMetrics();

        if (drawcursor) {
            g.setStroke(cursorStroke);
            int x = r.x + 10
                    + fm.stringWidth(searchString.substring(0, caretPosition))
                    + 1;
            final int height = fm.getHeight();
            g.drawLine(x, y - height + fm.getDescent(), x, y + fm.getDescent());
        }

        // draw results count
        if (searchString.length() > 0) {
            final int spacing = fm.getAscent();
            g.setFont(resultsfont);
            String drawStr = "";
            if (currentMatchIndex != -1) {
                drawStr = "match " + (currentMatchIndex + 1) + "/" + matchCount;
            } else {
                drawStr = matchCount + " matches";
            }
            g.drawString(drawStr, r.x + 10, y + spacing);
        }

    }

    private void drawStaticItems(Graphics2D g, final int bandtopy) {

        final Rectangle r = getDecorationBounds();
        // draw band
        g.setColor(background);
        g.fillRect(r.x, bandtopy, r.width, BAND_HEIGHT);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, Math.min(1.0f, transparency + 0.2f)));

        // draw mag glass
        int x = r.x + r.width - 55;
        int y = bandtopy + 18;
        g.setStroke(magglassStroke);
        g.drawOval(x, y, 30, 30);
        g.setStroke(maghandleStroke);
        g.drawLine(x + 27, y + 27, x + 37, y + 37);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparency));
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        repaint();
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public void setCaretPosition(int caretPosition) {
        this.caretPosition = caretPosition;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public void setCurrentMatchIndex(int currentMatchIndex) {
        this.currentMatchIndex = currentMatchIndex;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean isDrawingCursor() {
        return drawcursor;
    }


    /*
     * Used by the cursor Blinker Timer to store the state of the
     * cursor drawing action. if true, then cursor will be drawn in
     * the next cycle. 
     */
    public void drawCursor(boolean drawcursor) {
        this.drawcursor = drawcursor;
    }

    public void hideCursor() {
        this.drawcursor = false;
        cursorBlinkTimer.stop();
        repaint();
    }

    public void showCursor() {
        this.drawcursor = true;
        repaint();
        cursorBlinkTimer.start();

    }

    public boolean isUIVisible() {
        return visible;
    }

    public void setUIVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
        repaint();
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
        repaint();
    }

    public float getTransparency() {
        return transparency;
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
        repaint();
    }
}