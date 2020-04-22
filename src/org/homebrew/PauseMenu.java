package org.homebrew;

class PauseMenu extends GUIWithButtons
{
    private GameMain gm;
    public PauseMenu(GameMain gm)
    {
        this.gm = gm;
        addButton(250, 196, 140, 16, "Resume game");
        addButton(250, 220, 140, 16, "Save and resume game");
        addButton(250, 244, 140, 16, "Save and quit");
        addButton(250, 268, 140, 16, "Quit without saving");
    }
    public void onClick(int btn_id)
    {
        if(btn_id == 0)
            gm.showGUI(null);
        if(btn_id == 1)
        {
            gm.saveTo("jcraft.sav");
            gm.showGUI(null);
        }
        if(btn_id == 2)
        {
            gm.saveTo("jcraft.sav");
            gm.genHomeScreen();
            gm.showGUI(new StartMenu(gm));
        }
        if(btn_id == 3)
        {
            gm.genHomeScreen();
            gm.showGUI(new StartMenu(gm));
        }
    }
    public boolean doRotateCamera()
    {
        return false;
    }
    public void onkeydown(int key)
    {
        if(key == 27)
            gm.showGUI(null);
        else
            super.onkeydown(key);
    }
}
