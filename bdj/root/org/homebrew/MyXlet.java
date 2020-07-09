package org.homebrew;

import java.io.*;

import java.util.*;

import java.awt.*;
import java.net.*;

import javax.media.*;

import javax.tv.xlet.*;

import org.bluray.ui.event.HRcEvent;
import org.bluray.net.BDLocator;
import org.davic.net.Locator;

import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventListener;
import org.dvb.event.UserEventRepository;

import org.havi.ui.*;
import org.dvb.ui.*;

public class MyXlet implements Xlet, UserEventListener
{
    private HScene scene;
    private Screen gui;
    private XletContext context;
    private final ArrayList messages = new ArrayList();
    private int[] fb;
    private DVBBufferedImage img;
    private DVBBufferedImage img2;
    private GameMain game;
    public static class EventQueue
    {
        private LinkedList l;
        int cnt = 0;
        EventQueue()
        {
            l = new LinkedList();
        }
        public synchronized void put(Object obj)
        {
            l.addLast(obj);
            cnt++;
        }
        public synchronized Object get()
        {
            if(cnt == 0)
                return null;
            Object o = l.getFirst();
            l.removeFirst();
            cnt--;
            return o;
        }
    }
    private EventQueue eq;
    public void initXlet(XletContext context)
    {
        this.context = context;
        // START: Code required for text output. 
        scene = HSceneFactory.getInstance().getDefaultHScene();
        fb = new int[2*640*480];
        img = new DVBBufferedImage(640, 480);
        img2 = new DVBBufferedImage(640, 480);
        eq = new EventQueue();
        try
        {
            gui = new Screen(messages, img);
            gui.setSize(1920, 1080); // BD screen size
            scene.add(gui, BorderLayout.CENTER);
            // END: Code required for text output.
            UserEventRepository repo = new UserEventRepository("input");
            repo.addAllArrowKeys();
            repo.addAllColourKeys();
            repo.addAllNumericKeys();
            repo.addKey(HRcEvent.VK_ENTER);
            repo.addKey(HRcEvent.VK_POPUP_MENU);
            repo.addKey(19);
            repo.addKey(424);
            repo.addKey(425);
            repo.addKey(412);
            repo.addKey(417);
            EventManager.getInstance().addUserEventListener(this, repo);
            game = new GameMain(
                System.getProperty("dvb.persistent.root")
               +"/"+(String)context.getXletProperty("dvb.org.id")
               +"/"+(String)context.getXletProperty("dvb.app.id")
               +"/"
            );
            (new Thread()
            {
                public void run()
                {
                    try
                    {
                        while(true)
                        {
                            for(Integer i = (Integer)eq.get(); i != null; i = (Integer)eq.get())
                                game.keyEvent(i.intValue());
                            game.render(fb);
                            DVBGraphics g2d = img.createGraphics();
                            img2.setRGB(0, 0, 640, 480, fb, 640*480, 640);
                            g2d.drawImage(img2, 0, 0, 640, 480, 0, 0, 640, 480, null);
                            img2.setRGB(0, 0, 640, 480, fb, 0, 640);
                            g2d.drawImage(img2, 0, 0, 640, 480, 0, 0, 640, 480, null);
                            gui.gui = true;
                            scene.paint(scene.getGraphics());//scene.repaint();
                            Toolkit.getDefaultToolkit().sync();
                            Thread.yield();
                        }
                    }
                    catch(Throwable e)
                    {
                        gui.gui = false;
                        printStackTrace(e);
                        scene.repaint();
                    }
                }
            }).start();
        }
        catch(Throwable e)
        {
            printStackTrace(e);
        }
        scene.validate();
    }
    // Don't touch any of the code from here on.
    public void startXlet()
    {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();
    }
    public void pauseXlet()
    {
        gui.setVisible(false);
    }
    public void destroyXlet(boolean unconditional)
    {
        scene.remove(gui);
        scene = null;
    }
    private void printStackTrace(Throwable e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        if(trace.charAt(trace.length()-1) != '\n')
            trace += '\n';
        String line = "";
        for(int i = 0; i < trace.length(); i++)
        {
            char x = trace.charAt(i);
            if(x == '\n')
            {
                messages.add(line);
                line = "";
            }
            else
                line += x;
        }
    }
    public void userEventReceived(UserEvent evt)
    {
        if(evt.getType() == HRcEvent.KEY_PRESSED)
            eq.put(new Integer((int)evt.getCode()));
        else if(evt.getType() == HRcEvent.KEY_RELEASED)
            eq.put(new Integer(-(int)evt.getCode()));
    }
}
