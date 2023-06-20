import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

// A drawing area class that lets the user, and someone the user is connected to, draw on and display an image
public class DrawingArea extends JComponent {

    private Image image;
    private Graphics2D graphics2D;
    private Point oldPoint, newPoint;
    private Color currentColor = Color.black;
    private int currentThickness = 1;
    private ConnectionHandler connectionHandler;

    // DrawingArea constructor adds functionality when mouse button is pressed or mouse dragged inside the drawing area
    public DrawingArea() {
        addMouseListener(new drawOnMousePress());
        addMouseMotionListener(new drawOnMouseDrag());
    }

    // Draws the image to the drawing area when rePaint() is called
    public void paintComponent(Graphics g) {
        if (image == null) {
            // Makes an off-screen drawable image
            image = createImage(getSize().width, getSize().height);
            // Creates a reference to images graphics in order to draw to the image
            graphics2D = (Graphics2D) image.getGraphics();
            // Adds anti-aliasing
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fill the image with white
            localClear();
        }
        g.drawImage(image, 0, 0, null);
    }

    // Paints a line and sends the info of what was drawn to the connected client
    private void localDraw() {
        graphics2D.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);
        repaint();
        connectionHandler.sendDrawing(oldPoint, newPoint, currentColor, currentThickness);
    }

    // Gets called by the connected client to draw on the local drawing area
    public void externalDraw(Point oldPoint, Point newPoint, Color color, int thickness) {
        // Sets the color and thickness to what the other user has selected
        graphics2D.setPaint(color);
        graphics2D.setStroke(new BasicStroke(thickness));

        graphics2D.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);
        repaint();
        // Set color and thickness back to local settings
        graphics2D.setPaint(currentColor);
        graphics2D.setStroke(new BasicStroke(currentThickness));
    }

    // Clears the drawing area by filling it with white and sends the command to the connected user
    public void localClear() {
        graphics2D.setPaint(Color.white);
        graphics2D.fillRect(0, 0, getSize().width, getSize().height);
        repaint();
        connectionHandler.clearDrawing();
        // Set color back to what it was before pressing clear
        graphics2D.setColor(currentColor);
    }

    // Gets called by the connected client to clears the drawing area by filling it with white
    public void externalClear() {
        graphics2D.setPaint(Color.white);
        graphics2D.fillRect(0, 0, getSize().width, getSize().height);
        repaint();
        // Set color back to what it was before connected client pressed clear
        graphics2D.setColor(currentColor);
    }

    // Gets called by local GUI buttons to change the drawing color and saves it as the currently selected color
    public void setColor(String colorName) {
        try {
            Color color = (Color) Class.forName("java.awt.Color").getField(colorName.toLowerCase()).get(null);
            graphics2D.setPaint(color);
            currentColor = color;
        } catch (Exception e) {
            System.err.println("Button text is not a java.awt.Color");
        }
    }

    // Gets called by local spinner to set the drawing thickness and saves it as the currently selected thickness
    public void setThickness(int thickness) {
        graphics2D.setStroke(new BasicStroke(thickness));
        currentThickness = thickness;
    }

    // Setter method to create a reference from this object to a connectionHandler object created in the main class
    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    // When mouse is pressed the x and y-coordinates get saved as the starting point of the line to draw
    class drawOnMousePress extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            oldPoint = me.getPoint();
        }
    }

    // When the mouse is dragged this saves the x and y-coordinates as where to draw a line to and calls localDraw
    class drawOnMouseDrag extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent me) {
            newPoint = me.getPoint();
            localDraw();
            // Sets the new coordinates as the old coordinates so lines can keep being drawn
            oldPoint = newPoint;
        }
    }
}