package org.homebrew;

class StartMenu extends GUIWithButtons
{
    private GameMain gm;
    public StartMenu(GameMain gm)
    {
        this.gm = gm;
        addButton(250, 220, 140, 16, "Create new world");
        addButton(250, 244, 140, 16, "Load world", !LevelSave.saveExists("jcraft.sav"));
    }
    public void onClick(int btn_id)
    {
        if(btn_id == 0)
        {
            gm.genWorld((int)(System.currentTimeMillis()/1000));
            gm.showGUI(null);
        }
        if(btn_id == 1)
        {
            gm.loadFrom("jcraft.sav");
            gm.showGUI(null);
        }
    }
}
