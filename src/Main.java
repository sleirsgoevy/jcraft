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
    private boolean mouseCaptured;
    private Robot robot;
    private int offset_left;
    private int offset_top;
    private int offset_right;
    private int offset_bottom;
    private Cursor blankCursor;
    private Cursor oldCursor;
    public Main() throws Exception
    {
        super();
        game = new GameMain("");
        frame = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        robot = new Robot();
        Insets ins = getInsets();
        setSize(640+ins.left+ins.right, 480+ins.top+ins.bottom);
        offset_left = ins.left;
        offset_top = ins.top;
        offset_right = ins.right;
        offset_bottom = ins.bottom;
        resetMouse();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        oldCursor = getCursor();
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
                    if(game.doCaptureMouse() != mouseCaptured)
                    {
                        mouseCaptured = !mouseCaptured;
                        setCursor(mouseCaptured?blankCursor:oldCursor);
                        resetMouse();
                    }
                    Insets ins = getInsets();
                    if(ins.left != offset_left || ins.right != offset_right || ins.top != offset_top || ins.bottom != offset_bottom)
                    {
                        setSize(640+ins.left+ins.right, 480+ins.top+ins.bottom);
                        offset_left = ins.left;
                        offset_top = ins.top;
                        offset_right = ins.right;
                        offset_bottom = ins.bottom;
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
        g.drawImage(frame, offset_left, offset_top, 640+offset_left, 480+offset_top, 0, 0, 640, 480, null);
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
        boolean mc2 = game.doCaptureMouse();
        if(mc2 != mouseCaptured)
        {
            mouseCaptured = mc2;
            setCursor(mouseCaptured?blankCursor:oldCursor);
            resetMouse();
            return;
        }
        int x = e.getX();
        int y = e.getY();
        if(!mouseCaptured)
        {
            synchronized(game)
            {
                game.mouseMove(x-offset_left, y-offset_top);
            }
            return;
        }
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
        if(mouseCaptured)
            resetMouse();
    }
    public void mousePressed(MouseEvent e)
    {
        mouseMoved(e);
        synchronized(game)
        {
            game.mouseEvent(e.getButton());
        }
    }
    public void mouseReleased(MouseEvent e)
    {
        mouseMoved(e);
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
