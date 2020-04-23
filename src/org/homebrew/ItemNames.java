package org.homebrew;

class ItemNames
{
    private static final String[] names_low = {"Air"};
    private static final String[] names_high = {"Grass", "Wood", "Leaves", "Dirt", "Stone"};
    public static String getName(int i)
    {
        if(i >= 128)
            return names_high[i - 128];
        else if(i >= 0)
            return names_low[i];
        else
            return "Nothing";
    }
}
