package org.homebrew;

interface GUI
{
    public boolean doRotateCamera(); // minecraft-like start screen
    public boolean doCaptureMouse(); // show mouse if in gui (and not on console)
    public boolean doStopEngine(); // pause physics & main renderer while in gui
    public void onkeydown(int key);
    public void onkeyup(int key);
    public void mousemove(int x, int y); // relative or absolute, depending on doCaptureMouse()
    public void render(int[] buffer);
}
