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
