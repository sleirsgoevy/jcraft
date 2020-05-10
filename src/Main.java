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
    private Object[] getGamepads()
    {
        try
        {
            return net.java.games.input.ControllerEnvironment.getDefaultEnvironment().getControllers();
        }
        catch(NoClassDefFoundError e) // no jinput in classpath
        {
            return new Object[0];
        }
    }
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
        oldCursor = getCursor();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        setLayout(null);
        setVisible(true);
        (new Thread()
        {
            public void run()
            {
                String X, Y, RX, RY;
                if(System.getenv("DONT_SWAP_STICKS_PLZ") != null)
                {
                    X = "rx";
                    Y = "ry";
                    RX = "x";
                    RY = "y";
                }
                else
                {
                    X = "x";
                    Y = "y";
                    RX = "rx";
                    RY = "ry";
                }
                Object[] gamepads = getGamepads();
                double left_x = 0, left_y = 0, right_x = 0, right_y = 0;
                int pov = 0;
                long prev_time = System.currentTimeMillis();
		while(true)
                {
                    synchronized(game)
                    {
                        game.render(fb);
                    }
                    for(int i = 0; i < gamepads.length; i++)
                    {
                        net.java.games.input.Controller g = (net.java.games.input.Controller)gamepads[i];
                        net.java.games.input.Event e = new net.java.games.input.Event();
                        g.poll();
                        while(g.getEventQueue().getNextEvent(e))
                        {
                            String name = e.getComponent().getName();
                            double val = e.getValue();
                            if(name.equals(X))
                                left_x = val;
                            else if(name.equals(Y))
                                left_y = val;
                            else if(name.equals(RX))
                                right_x = val;
                            else if(name.equals(RY))
                                right_y = -val;
                            else if(name.equals("pov"))
                            {
                                pov = (int)(val*8);
                                synchronized(game)
                                {
                                    game.keyEvent((pov==1||pov>=7?1:-1)*37);
                                    game.keyEvent((pov>=1&&pov<=3?1:-1)*38);
                                    game.keyEvent((pov>=3&&pov<=5?1:-1)*39);
                                    game.keyEvent((pov>=5&&pov<=7?1:-1)*40);
                                }
                            }
                            else
                            {
                                int key = -1;
                                if(name.equals("Left Thumb"))
                                    key = 424;
                                else if(name.equals("Right Thumb"))
                                    key = 425;
                                else if(name.equals("Left Thumb 2"))
                                    key = 412;
                                else if(name.equals("Right Thumb 2"))
                                    key = 417;
                                else if(name.equals("A"))
                                    key = 10;
                                else if(name.equals("B"))
                                    key = 19;
                                else if(name.equals("Y"))
                                    key = 461;
                                if(key >= 0)
                                {
                                    if(val == 0)
                                        key = -key;
                                    synchronized(game)
                                    {
                                        game.keyEvent(key);
                                    }
                                }
                            }
                        }
                    }
                    long cur_time = System.currentTimeMillis();
                    synchronized(game)
                    {
                        if(game.doCaptureMouse())
                        {
                            game.mouseMove((int)((cur_time-prev_time)*left_x), (int)((cur_time-prev_time)*left_y));
                            game.rightStick(right_x, right_y);
                        }
                        else if(pov == 0)
                        {
                            game.keyEvent((left_x==-1?1:-1)*37);
                            game.keyEvent((left_y==-1?1:-1)*38);
                            game.keyEvent((left_x==1?1:-1)*39);
                            game.keyEvent((left_y==1?1:-1)*40);
                        }
                    }
                    prev_time = cur_time;
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
        lastMouseX = offset_left+320;
        lastMouseY = offset_top+240;
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
        if(x < 260 || x > 380 || y < 180 || y > 300)
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
