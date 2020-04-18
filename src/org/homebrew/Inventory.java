package org.homebrew;

class Inventory
{
    private static int[] hotbar = {128, 131, 129, 130, -1, -1, -1, -1, -1};
    private int hotbarSlot;
    public Inventory()
    {
        hotbarSlot = 0;
    } 
    public void setHotbarSlot(int x)
    {
        hotbarSlot = x % hotbar.length;
    }
    public int getHotbarSlot()
    {
        return hotbarSlot;
    }
    public int getHotbarItem()
    {
        return hotbar[hotbarSlot];
    }
    public void renderHotbar(int[] buffer)
    {
        String s = "Held item: "+ItemNames.getName(getHotbarItem());
        int offset = 640 * 464 + (640 - FontRenderer.stringWidth(s))/2;
        FontRenderer.renderString(buffer, offset, s, 0, 0xffffff);
    }
}
