package org.homebrew;

import java.util.ArrayList;

public class GUIWithButtons implements GUI
{
    private java.util.ArrayList buttons;
    private int mouseFocus;
    private int keyboardFocus;
    private boolean haveEnabledButtons;
    private class Button
    {
        public int x;
        public int y;
        public int w;
        public int h;
        public String text;
        public boolean disabled;
        public Button(int x, int y, int w, int h, String text, boolean disabled)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.text = text;
            this.disabled = disabled;
        }
    }
    public GUIWithButtons()
    {
        buttons = new ArrayList();
        mouseFocus = keyboardFocus = -1;
        haveEnabledButtons = false;
    }
    protected void addButton(int x, int y, int w, int h, String text, boolean disabled)
    {
        buttons.add(new Button(x, y, w, h, text, disabled));
        if(!disabled)
            haveEnabledButtons = true;
    }
    protected void addButton(int x, int y, int w, int h, String text)
    {
        addButton(x, y, w, h, text, false);
    }
    public void mousemove(int x, int y) //callback
    {
        mouseFocus = -1;
        for(int i = 0; i < buttons.size(); i++)
        {
            Button b = (Button)buttons.get(i);
            if(b.disabled)
                continue;
            if(x >= b.x && x < b.x+b.w && y >= b.y && y < b.y+b.h)
                mouseFocus = i;
        }
        keyboardFocus = mouseFocus;
    }
    public void onkeydown(int key) //callback
    {
        if(key == 38 && haveEnabledButtons)
        {
            do
            {
                if(--keyboardFocus < 0)
                    keyboardFocus = buttons.size() - 1;
            }
            while(((Button)buttons.get(keyboardFocus)).disabled);
        }
        if(key == 40 && haveEnabledButtons)
        {
            do
            {
                if(++keyboardFocus >= buttons.size())
                    keyboardFocus = 0;
            }
            while(((Button)buttons.get(keyboardFocus)).disabled);
        }
        if(key == 10)
        {
            if(keyboardFocus >= 0)
                onClick(keyboardFocus);
        }
        if(key == 1001)
        {
            if(keyboardFocus == mouseFocus)
            {
                if(mouseFocus >= 0)
                    onClick(mouseFocus);
            }
            else
                keyboardFocus = mouseFocus;
        }
    }
    public void onkeyup(int key){} //callback
    public void onClick(int button_id){}
    public void render(int[] buffer)
    {
        for(int i = 0; i < buttons.size(); i++)
        {
            int color = 0xff808080;
            if(i == keyboardFocus)
                color = 0xffc0c0c0;
            Button b = (Button)buttons.get(i);
            if(b.disabled)
                color = 0xffa0a0a0;
            for(int x = b.x; x < b.x + b.w; x++)
                for(int y = b.y; y < b.y + b.h; y++)
                    buffer[640*y+x] = color;
            int startx = b.x + (b.w - FontRenderer.stringWidth(b.text)) / 2;
            int starty = b.y + (b.h - 8) / 2;
            FontRenderer.renderString(buffer, starty*640+startx, b.text, -1, b.disabled?0x4f4f4f:0xffffff); // gray or black
        }
    }
    public boolean doRotateCamera()
    {
        return true;
    }
    public boolean doCaptureMouse()
    {
        return false;
    }
}
