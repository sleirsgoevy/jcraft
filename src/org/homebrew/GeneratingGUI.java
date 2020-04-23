package org.homebrew;

class GeneratingGUI implements GUI
{
    private double percent;
    private double scale;
    public GeneratingGUI()
    {
        percent = 1;
        scale = 1;
    }
    public synchronized void reportProgress(double percent)
    {
        System.out.println((100*percent)+"%");
        percent = 1 - percent;
        if(percent > this.scale * this.percent)
            this.scale *= this.percent / percent;
        this.percent = percent;
    }
    public synchronized void render(int[] buffer)
    {
        // draw dirt texture
        for(int i = 0; i < 640*480; i++)
        {
            int x = i % 640 / 2 % 16;
            int y = i / 640 / 2 % 16;
            buffer[i] = TextureAtlas.atlas[256*y+x+32];
        }
        int text_offset = 640*230+(640-FontRenderer.stringWidth("Generating terrain..."))/2;
        FontRenderer.renderString(buffer, text_offset, "Generating terrain...", -1, 0xffffff); // black
        int pc = (int)Math.floor((1 - percent * scale) * 100);
        // draw progress bar
        for(int i = 0; i < 100; i++)
        {
            int color = (i<pc?0xff00ff00:0xff000000); // green or black
            buffer[640*239+270+i] = buffer[640*240+270+i] = color;
        }
    }
    public boolean doRotateCamera()
    {
        return false;
    }
    public boolean doCaptureMouse()
    {
        return false;
    }
    public boolean doStopEngine()
    {
        return true;
    }
    public void onkeydown(int key){}
    public void onkeyup(int key){}
    public void mousemove(int x, int y){}
}
