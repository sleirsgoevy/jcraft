package org.homebrew;

import java.util.Random;

class Life3d
{
    public static void life3d(Random r, byte[] world, byte[] world0, int[] aux, GeneratingGUI report)
    {
        for(int i = 0; i < 128*128*128; i++)
            world[i] = (byte)r.nextInt(2);
        int auxp = -1;
        while(auxp != 0)
        {
            int auxp2 = (auxp<0?0:auxp);
            int modcnt = 0;
            for(int i = 0; i < (auxp>=0?auxp:128*128*128); i++)
            {
                int cur = (auxp>=0?aux[i]:i);
                int x = cur >> 14;
                int z = (cur >> 7) & 0x7f;
                int y = cur & 0x7f;
                int cnt = 0;
                for(int xi = x + 127; xi <= x + 129; xi++)
                for(int yi = y + 127; yi <= y + 129; yi++)
                for(int zi = z + 127; zi <= z + 129; zi++)
                    if(world[16384*(xi%128)+128*(zi%128)+(yi%128)] != 0)
                        cnt++;
                if((world[16384*x+128*z+y] != 0 && cnt >= 9) || cnt >= 18)
                    world0[16384*x+128*z+y] |= 1;
                else
                    world0[16384*x+128*z+y] &= ~1;
                if((world[16384*x+128*z+y]&1) != (world0[16384*x+128*z+y]&1))
                {
                    modcnt++;
                    for(int xi = x + 127; xi <= x + 129; xi++)
                    for(int yi = y + 127; yi <= y + 129; yi++)
                    for(int zi = z + 127; zi <= z + 129; zi++)
                    {
                        if((world0[16384*(xi%128)+128*(zi%128)+(yi%128)]>>1) == 0 && auxp2 >= 0)
                        {
                            aux[auxp2++] = 16384*(xi%128)+128*(zi%128)+(yi%128);
                            if(auxp2 >= 500000)
                                auxp2 = -1;
                        }
                        world0[16384*(xi%128)+128*(zi%128)+(yi%128)] |= 4;
                    }
                }
            }
            System.out.println("auxp2 = "+auxp2);
            System.out.println("modcnt = "+modcnt);
            double percent = 1 - Math.log(modcnt/30000.0) / Math.log(5/3.);
            if(report != null)
                report.reportProgress(percent);
            if(auxp2 < 0)
            {
                for(int i = 0; i < 128*128*128; i++)
                {
                    world0[i] &= 1;
                    world[i] = world0[i];
                }
                auxp = -1;
                continue;
            }
            auxp = 0;
            for(int i = 0; i < auxp2; i++)
            {
                world[aux[i]] = (byte)(world0[aux[i]] & 1);
                world0[aux[i]] = (byte)((world0[aux[i]]&1)|((world0[aux[i]]>>1)&2));
                if((world0[aux[i]] >> 1) != 0)
                    aux[auxp++] = aux[i];
            }
        }
    }
}
