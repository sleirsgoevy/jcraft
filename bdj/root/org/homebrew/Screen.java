package org.homebrew;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class Screen extends Container
{
    private static final long serialVersionUID = 4761178503523947426L;
    private ArrayList messages;
    private Font font;
    public boolean gui;
    private org.dvb.ui.DVBBufferedImage img;
    private long prev_time;
    public Screen(ArrayList messages, org.dvb.ui.DVBBufferedImage img)
    {
        this.messages = messages;
        font = new Font(null, Font.PLAIN, 36);
        gui = false;
        this.img = img;
        prev_time = System.currentTimeMillis();
    }
    public void paint(Graphics g)
    {
        long cur_time = System.currentTimeMillis();
        if(gui)
        {
            g.drawImage(img, 0, 0, 1920, 1080, 0, 0, 640, 480, null);
            g.setFont(font);
            g.setColor(new Color(0, 0, 0));
            g.drawString(1000./(cur_time-prev_time)+" FPS", 0, 50);
            g.setColor(new Color(255, 255, 255));
        }
        else
        {
            g.setFont(font);
            g.setColor(new Color(100, 110, 160));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255));
            int top = 100;
            for(int i = 0; i < messages.size(); i++)
            {
                String message = (String) messages.get(i);
                int message_width = g.getFontMetrics().stringWidth(message);
                g.drawString(message, 0, top + (i*40));
            }
        }
        prev_time = cur_time;
    }
}
