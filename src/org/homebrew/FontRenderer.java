package org.homebrew;

class FontRenderer
{
    public static int stringWidth(String s)
    {
        int w = s.length() - 1;
        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(c >= 128)
                throw new RuntimeException("cannot render non-ASCII characters");
            w += FontBundle.shifts[c+1] - FontBundle.shifts[c];
        }
        return w;
    }
    /*
    for each pixel that is obscured by letters, the formula `color = (color | orMask) ^ xorMask` is applied.
    * to keep a bit intact, set that bit to 0 0 in orMask and xorMask
    * to invert a bit, set that bit to 0 1
    * to set a bit to 1, set that bit to 1 0
    * to set a bit to 0, set that bit to 1 1
    examples:
    * set alpha (high byte) to all ones, invert rgb (low 3 bytes): orMask = 0xff000000, xorMask = 0x00ffffff
    * set alpha (high byte) to all ones, rgb to all zeros (black): orMask = 0xffffffff, xorMask = 0x00ffffff
    */
    public static void renderString(int[] buffer, int offset, String s, int orMask, int xorMask)
    {
        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(c >= 128)
                throw new RuntimeException("cannot render non-ASCII characters");
            for(int x = FontBundle.shifts[c]; x < FontBundle.shifts[c+1]; x++)
            {
                for(int y = 0; y < 8; y++)
                    if((FontBundle.letters[x] & (1 << y)) != 0)
                        buffer[offset+640*y] = (buffer[offset+640*y] | orMask) ^ xorMask;
                offset++;
            }
            offset++;
        }
    }
}
