import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// A connection handler class that sends what is drawn locally and receives what is drawn by another client
public class ConnectionHandler {

    private final String host;
    private final int myPort;
    private final int remotePort;
    private DrawingArea drawingArea;

    // ConnectionHandler constructor takes connection info and saves it, and creates and starts a thread that listens
    // for incoming drawing info
    public ConnectionHandler(int myPort, String host, int remotePort) {
        this.myPort = myPort;
        this.host = host;
        this.remotePort = remotePort;

        new Thread(listenDrawing).start();
    }

    // Takes the coordinates to draw a line between, the color and thickness of the line, opens a datagram socket to
    // the other client, makes the info into a string and then into bytes and sends it over the socket
    public void sendDrawing(Point oldPoint, Point newPoint, Color color, int thickness) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String infoString = oldPoint.x + " " + oldPoint.y + " "
                    + newPoint.x + " " + newPoint.y + " "
                    + color.getRGB() + " " + thickness;
            byte[] bytes = infoString.getBytes();
            InetAddress address = InetAddress.getByName(this.host);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, remotePort);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Couldn't send packet " + e);
        }
    }

    // Send a string turned into bytes that says to clear the other drawingArea
    public void clearDrawing() {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] bytes = "clear".getBytes();
            InetAddress address = InetAddress.getByName(this.host);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, remotePort);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Couldn't send packet " + e);
        }
    }

    // Runnable that listens to bytes from the other client
    Runnable listenDrawing = new Runnable() {
        public void run() {
            // Opens a datagram socket to the other client
            try (DatagramSocket socket = new DatagramSocket(myPort)) {
                while (true) {
                    // Receives bytes and turns them into a string
                    byte[] bytes = new byte[8000];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    String infoString = new String(packet.getData(), 0, packet.getLength());
                    // If the string contains a command to clear the drawing area, clear it
                    if (infoString.equals("clear")) {
                        drawingArea.externalClear();
                    } else {
                        // Split the incoming string at each space and turn its pieces into two points for drawing a
                        // line between, it's color and thickness
                        String[] infoStrings = infoString.split(" ");
                        Point oldPoint = new Point(Integer.parseInt(infoStrings[0]), Integer.parseInt(infoStrings[1]));
                        Point newPoint = new Point(Integer.parseInt(infoStrings[2]), Integer.parseInt(infoStrings[3]));
                        Color color = new Color(Integer.parseInt(infoStrings[4]));
                        int thickness = Integer.parseInt(infoStrings[5]);
                        // Tell the drawArea to draw the line
                        drawingArea.externalDraw(oldPoint, newPoint, color, thickness);
                    }
                }
            } catch (IOException e) {
                System.err.println("Couldn't receive packet " + e);
            }
        }
    };

    // Setter method to create a reference from this object to a drawing area object created in the main class
    public void setDrawArea(DrawingArea drawingArea) {
        this.drawingArea = drawingArea;
    }
}
