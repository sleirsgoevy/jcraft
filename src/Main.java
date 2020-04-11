import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import org.homebrew.GameMain;

import javax.imageio.ImageIO;

public class Main extends Frame implements KeyListener, WindowListener
{
    private GameMain game;
    private BufferedImage frame;
    private int[] fb = new int[640*480];
    public Main() throws Exception
    {
        super();
        game = new GameMain();
        frame = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        setSize(640, 480);
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
    public void windowActivated(WindowEvent e){}
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
    public static void main(String[] argv) throws Exception
    {
        new Main();
    }
}
