import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import org.homebrew.GameMain;

import javax.imageio.ImageIO;

public class Main extends Frame implements KeyListener, WindowListener, MouseListener, MouseMotionListener, MouseWheelListener
{
    private GameMain game;
    private BufferedImage frame;
    private int[] fb = new int[640*480];
    private int lastMouseX;
    private int lastMouseY;
    private Robot robot;
    public Main() throws Exception
    {
        super();
        game = new GameMain();
        frame = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        robot = new Robot();
        setSize(640, 480);
        resetMouse();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);
        setLayout(null);
        setVisible(true);
        (new Thread()
        {
            public void run()
            {
                while(true)
                {
                    synchronized(game)
                    {
                        game.render(fb);
                    }
                    my_paint(getGraphics());
                }
            }
        }).start();
        addKeyListener(this);
        addWindowListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    public void my_paint(Graphics g)
    {
        frame.setRGB(0, 0, 640, 480, fb, 0, 640);
        g.drawImage(frame, 0, 0, 640, 480, 0, 0, 640, 480, null);
    }
    public void keyPressed(KeyEvent e)
    {
        synchronized(game)
        {
            game.keyEvent(e.getKeyCode());
        }
    }
    public void keyReleased(KeyEvent e)
    {
        synchronized(game)
        {
            game.keyEvent(-e.getKeyCode());
        }
    }
    public void keyTyped(KeyEvent e){}
    public void windowActivated(WindowEvent e)
    {
        resetMouse();
    }
    public void windowClosed(WindowEvent e)
    {
        System.exit(0);
    }
    public void windowClosing(WindowEvent e)
    {
        System.exit(0);
    }
    public void windowDeactivated(WindowEvent e){}
    public void windowDeiconified(WindowEvent e){}
    public void windowIconified(WindowEvent e){}
    public void windowOpened(WindowEvent e){}
    private synchronized void resetMouse()
    {
        lastMouseX = 320;
        lastMouseY = 240;
        robot.mouseMove(getX()+lastMouseX, getY()+lastMouseY);
    }
    public synchronized void mouseMoved(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        int diffx = x - lastMouseX;
        int diffy = y - lastMouseY;
        lastMouseX = x;
        lastMouseY = y;
        synchronized(game)
        {
            game.mouseMove(diffx, diffy);
        }
        if(x < 160 || x > 480 || y < 120 || y > 360)
            resetMouse();
    }
    public void mouseDragged(MouseEvent e)
    {
        mouseMoved(e);
    }
    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e)
    {
        mouseMoved(e);
        resetMouse();
    }
    public void mousePressed(MouseEvent e)
    {
        synchronized(game)
        {
            game.mouseEvent(e.getButton());
        }
    }
    public void mouseReleased(MouseEvent e)
    {
        synchronized(game)
        {
            game.mouseEvent(-e.getButton());
        }
    }
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        synchronized(game)
        {
            game.mouseWheel(e.getWheelRotation());
        }
    }
    public static void main(String[] argv) throws Exception
    {
        new Main();
    }
}
