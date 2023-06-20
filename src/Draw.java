import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

// Main class (and main frame for GUI) for UDP drawing application that lets two clients draw on the same image
public class Draw extends JFrame{

    private static String host = "localhost";
    private static int myPort = 2000;
    private static int remotePort = 2001;

    private final DrawingArea drawingArea;
    private final JPanel leftPanel = new JPanel();

    // Main method takes arguments for port to use, host and port to connect to, and creates a main class instance
    public static void main(String[] args) {
        if (args.length > 0) {
            myPort = Integer.parseInt(args[0]);
        } if (args.length > 1) {
            host = args[1];
        } if (args.length > 2) {
            remotePort = Integer.parseInt(args[2]);
        }
        new Draw();
    }

    // Main class instance creates a GUI for drawing program and sets up buttons and functionality
    public Draw() {
        setTitle("Draw together");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Creates a connection handler and a drawing area and gives them references to each other
        ConnectionHandler connectionHandler = new ConnectionHandler(myPort, host, remotePort);
        drawingArea = new DrawingArea();
        connectionHandler.setDrawArea(drawingArea);
        drawingArea.setConnectionHandler(connectionHandler);

        getContentPane().add(drawingArea, BorderLayout.CENTER);
        getContentPane().add(leftPanel, BorderLayout.WEST);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        makeButtons();
        makeSpinner();

        // Show GUI
        setSize(800, 600);
        setResizable(false);
        setVisible(true);
    }

    // Makes all buttons, adds functionality to them, sets their size and adds them to the frame
    private void makeButtons() {
        ArrayList<JButton> buttons = new ArrayList<>(Arrays.asList(new JButton("Clear"),
                new JButton("Eraser"), new JButton("Black"), new JButton("Red"),
                new JButton("Orange"), new JButton("Yellow"), new JButton("Green"),
                new JButton("Cyan"), new JButton("Blue"), new JButton("Magenta"),
                new JButton("Pink")));

        for (JButton button : buttons) {
            button.setMaximumSize(new Dimension(100, 30));
            button.addActionListener(buttonAction);
            leftPanel.add(button);
        }
    }

    // Makes a spinner, adds functionality that lets the user choose thickness, sets its size and adds it to the frame
    private void makeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spinner.addChangeListener(e -> {
            String thickness = spinner.getValue() + "";
            drawingArea.setThickness(Integer.parseInt(thickness));
        });

        spinner.setMaximumSize(new Dimension(80, 30));
        spinner.setAlignmentX(LEFT_ALIGNMENT);

        leftPanel.add(new JLabel("Thickness"));
        leftPanel.add(spinner);
    }

    // When any button is pressed this checks which button it is and changes drawing color to the text on the button,
    // (white if the button says "Erase") or clears the drawing area if the button says "Clear"
    ActionListener buttonAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton button) {

                if (button.getText().equals("Clear")) {
                    drawingArea.localClear();

                } else if (button.getText().equals("Eraser")) {
                    drawingArea.setColor("white");

                } else {
                    drawingArea.setColor(button.getText());
                }
            }
        }
    };
}
