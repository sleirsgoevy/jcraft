package org.homebrew;

import java.util.Random;

public class GameMain
{
    final static int[] block_colors = {0xff00ff00 /* grass */, 0xff800000 /* wood */, 0xff008000 /* leaves */, 0xffff8000 /* dirt */};
    private byte[] world;
    private double playerX;
    private double playerY;
    private double playerZ;
    private double playerYaw;
    private double playerYaw_cos;
    private double playerYaw_sin;
    private double playerPitch;
    private double playerPitch_cos;
    private double playerPitch_sin;
    private byte[] preflight;
    private int[] dsu;
    private boolean[] keyStates;
    private int vel_x;
    private double vel_y;
    private int vel_z;
    private int vel_yaw;
    private int vel_pitch;
    private long prev_time;
    public GameMain()
    {
        world = new byte[128*128*128];
        MapGen.generate(world, 179);
        playerX = playerZ = 64;
        playerY = 127;
        playerYaw = playerYaw_sin = playerPitch_cos = 0;
        playerPitch = -Math.PI/2;
        playerYaw_cos = 1;
        playerPitch_sin = -1;
        vel_y = vel_x = vel_z = 0;
        preflight = new byte[(128*128*128)/8];
        dsu = new int[640*480];
        keyStates = new boolean[1024];
        prev_time = System.currentTimeMillis();
    }
    public void render(int[] buffer)
    {
        for(int i = 0; i < preflight.length; i++)
            preflight[i] = 0;
        int px = (int)playerX;
        int py = (int)playerY;
        int pz = (int)playerZ;
        dfs_preflight(preflight, world, px, py, pz, px, py, pz);
        for(int i = 0; i < 640*480; i++)
            buffer[i] = 0;
        for(int i = 0; i < 640*480; i++)
            dsu[i] = i + 1;
        for(int i = px; i >= 0; i--)
        {
            for(int j = py; j >= 0; j--)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
            for(int j = py + 1; j < 128; j++)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
        }
        for(int i = px + 1; i < 128; i++)
        {
            for(int j = py; j >= 0; j--)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
            for(int j = py + 1; j < 128; j++)
            {
                for(int k = pz; k >= 0; k--)
                    render_block(buffer, i, j, k);
                for(int k = pz + 1; k < 128; k++)
                    render_block(buffer, i, j, k);
            }
        }
        for(int i = 0; i < 640*480; i++)
            if(buffer[i] == 0)
                buffer[i] = -1;
        playerPhysics();
    }
    private void playerPhysics()
    {
        long cur_time = System.currentTimeMillis();
        for(long tick = prev_time; tick < cur_time; tick++)
        {
            double playerX_prev = playerX;
            double playerY_prev = playerY;
            double playerZ_prev = playerZ;
            playerYaw += vel_yaw / 250.0;
            if(playerYaw > 2*Math.PI)
                playerYaw -= 2*Math.PI;
            else if(playerYaw < 0)
                playerYaw += 2*Math.PI;
            playerYaw_cos = Math.cos(playerYaw);
            playerYaw_sin = Math.sin(playerYaw);
            playerPitch += vel_pitch / 250.0;
            if(playerPitch > Math.PI/2)
                playerPitch = Math.PI/2;
            else if(playerPitch < -Math.PI/2)
                playerPitch = -Math.PI/2;
            playerPitch_cos = Math.cos(playerPitch);
            playerPitch_sin = Math.sin(playerPitch);
            playerX += vel_x * playerYaw_cos / 250.0 + vel_z * playerYaw_sin / 250.0;
            if(playerX < 0)
                playerX = 0;
            if(playerX >= 128)
                playerX = 127.999999;
            playerY += vel_y / 250.0;
            if(playerY < 0)
                playerY = 0;
            if(playerY >= 128)
                playerY = 127.999999;
            vel_y -= 0.004;
            playerZ += vel_z * playerYaw_cos / 250.0 - vel_x * playerYaw_sin / 250.0;
            if(playerZ < 0)
                playerZ = 0;
            if(playerZ >= 128)
                playerZ = 127.999999;
            int px = (int)Math.floor(playerX);
            if(px < 1)
                px = 1;
            if(px > 126)
                px = 126;
            int py = (int)Math.floor(playerY);
            if(py < 4)
                py = 4;
            if(py > 126)
                py = 126;
            int pz = (int)Math.floor(playerZ);
            if(pz < 1)
                pz = 1;
            if(pz > 126)
                pz = 126;
            for(int y = py - 4; y <= py + 1; y++)
                for(int x = px - 1; x <= px + 1; x++)
                    for(int z = pz - 1; z <= pz + 1; z++)
                    {
                        int pos = 16384*x+128*z+y;
                        if(world[pos] >= 0) // byte is signed, so >=0 means <128
                            continue;
                        if(playerX <= x - 0.3 || playerX >= x + 1.3
                        || playerY <= y - 0.3 || playerY > y + 2.6
                        || playerZ <= z - 0.3 || playerZ >= z + 1.3)
                            continue;
                        boolean prevX_ok = (playerX_prev <= x - 0.3 || playerX_prev >= x + 1.3);
                        boolean prevY_ok = (playerY_prev <= y - 0.3 || playerY_prev >= y + 2.6);
                        boolean prevZ_ok = (playerZ_prev <= z - 0.3 || playerZ_prev >= z + 1.3);
                        if(prevY_ok && playerY <= y + 0.5 && (y > 0 && world[pos-1] >= 0))
                        {
                            playerY = y - 0.3;
                            vel_y = 0;
                        }
                        else if(prevY_ok && playerY >= y + 0.5 && (y < 127 && world[pos+1] >= 0))
                        {
                            playerY = y + 2.6;
                            vel_y = 0;
                            if(keyStates[32] || keyStates[10])
                                vel_y = 1.5;
                        }
                        else if(prevX_ok && playerX <= x + 0.5 && (x > 0 && world[pos-16384] >= 0))
                            playerX = x - 0.3;
                        else if(prevX_ok && playerX >= x + 0.5 && (x < 127 && world[pos+16384] >= 0))
                            playerX = x + 1.3;
                        else if(prevZ_ok && playerZ <= z + 0.5 && (z > 0 && world[pos-128] >= 0))
                            playerZ = z - 0.3;
                        else if(prevZ_ok && playerZ >= z + 0.5 && (z < 127 && world[pos+128] >= 0))
                            playerZ = z + 1.3;
                    }
        }
        prev_time = cur_time;
    }
    private void dfs_preflight(byte[] preflight, byte[] world, int x, int y, int z, int px, int py, int pz)
    {
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) != 0)
            return;
        preflight[idx] = (byte)(preflight[idx] | mask);
        if((world[pos] & 128) != 0) // can't see through solid blocks
            return;
        // ycos       0    -ysin
        // -ysin*psin pcos -ycos*psin
        // ysin*pcos  psin ycos*pcos
        double c1, c2, c3;
        c1 = playerYaw_sin*playerPitch_cos;
        c2 = playerPitch_sin;
        c3 = playerYaw_cos*playerPitch_cos;
        double zz = 0;
        if(c1 > 0)
            zz += (x+1-playerX)*c1;
        else
            zz += (x-playerX)*c1;
        if(c2 > 0)
            zz += (y+1-playerY)*c2;
        else
            zz += (y-playerY)*c2;
        if(c3 > 0)
            zz += (z+1-playerZ)*c3;
        else
            zz += (z-playerZ)*c3;
        if(zz < 0) // block is fully invisible
            return;
        if(point_out_of_screen(x, y, z)
        && point_out_of_screen(x+1, y, z)
        && point_out_of_screen(x, y+1, z)
        && point_out_of_screen(x+1, y+1, z)
        && point_out_of_screen(x, y, z+1)
        && point_out_of_screen(x+1, y, z+1)
        && point_out_of_screen(x, y+1, z+1)
        && point_out_of_screen(x+1, y+1, z+1)) // block is in front of the player but outside of the frame
            return;
        if(x != 127 && x >= px)
            dfs_preflight(preflight, world, x+1, y, z, px, py, pz);
        if(x != 0 && x <= px)
            dfs_preflight(preflight, world, x-1, y, z, px, py, pz);
        if(y != 127 && y >= py)
            dfs_preflight(preflight, world, x, y+1, z, px, py, pz);
        if(y != 0 && y <= py)
            dfs_preflight(preflight, world, x, y-1, z, px, py, pz);
        if(z != 127 && z >= pz)
            dfs_preflight(preflight, world, x, y, z+1, px, py, pz);
        if(z != 0 && z <= pz)
            dfs_preflight(preflight, world, x, y, z-1, px, py, pz);
    }
    private boolean point_out_of_screen(double x, double y, double z)
    {
        x -= playerX;
        y -= playerY;
        z -= playerZ;
        double tmp;
        tmp = x * playerYaw_cos - z * playerYaw_sin;
        z = x * playerYaw_sin + z * playerYaw_cos;
        x = tmp;
        tmp = z * playerPitch_cos + y * playerPitch_sin;
        y = y * playerPitch_cos - z * playerPitch_sin;
        z = tmp;
        x = 320 + (x / z) * 250;
        y = 240 - (y / z) * 250;
        return x < 0 || x > 640 || y < 0 || y > 480;
    }
    private void render_block(int[] buffer, int x, int y, int z)
    {
        int pos = x*16384+z*128+y;
        int idx = pos >> 3;
        int mask = 1 << (pos & 7);
        if((preflight[idx] & mask) == 0)
            return;
        int block = 255&(int)world[x*16384+z*128+y];
        int color = -1;
        if(block == 0)
            return; //air
        else
            color = block_colors[block - 128];
        boolean outline = (x-playerX)*(x-playerX)+(y-playerY)*(y-playerY)+(z-playerZ)*(z-playerZ)<=625;
        if(playerY < y && world[pos-1] < 128)
            render_plane(buffer, x, y, z, x+1, y, z, x+1, y, z+1, x, y, z+1, color, outline);
        if(playerY > y+1 && world[pos+1] < 128)
	    render_plane(buffer, x, y+1, z, x+1, y+1, z, x+1, y+1, z+1, x, y+1, z+1, color, outline);
        if(playerX < x && world[pos-16384] < 128)
            render_plane(buffer, x, y, z, x, y+1, z, x, y+1, z+1, x, y, z+1, color, outline);
        if(playerX > x+1 && world[pos+16384] < 128)
            render_plane(buffer, x+1, y, z, x+1, y+1, z, x+1, y+1, z+1, x+1, y, z+1, color, outline);
        if(playerZ < z && world[pos-128] < 128)
            render_plane(buffer, x, y, z, x+1, y, z, x+1, y+1, z, x, y+1, z, color, outline);
        if(playerZ > z+1 && world[pos+128] < 128)
            render_plane(buffer, x, y, z+1, x+1, y, z+1, x+1, y+1, z+1, x, y+1, z+1, color, outline);
    }
    private void render_plane(int[] buffer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int color, boolean outline)
    {
        x1 -= playerX;
        x2 -= playerX;
        x3 -= playerX;
        x4 -= playerX;
        y1 -= playerY;
        y2 -= playerY;
        y3 -= playerY;
        y4 -= playerY;
        z1 -= playerZ;
        z2 -= playerZ;
        z3 -= playerZ;
        z4 -= playerZ;
        double tmp;
        tmp = x1 * playerYaw_cos - z1 * playerYaw_sin;
        z1 = x1 * playerYaw_sin + z1 * playerYaw_cos;
        x1 = tmp;
        tmp = z1 * playerPitch_cos + y1 * playerPitch_sin;
        y1 = y1 * playerPitch_cos - z1 * playerPitch_sin;
        z1 = tmp;
        tmp = x2 * playerYaw_cos - z2 * playerYaw_sin;
        z2 = x2 * playerYaw_sin + z2 * playerYaw_cos;
        x2 = tmp;
        tmp = z2 * playerPitch_cos + y2 * playerPitch_sin;
        y2 = y2 * playerPitch_cos - z2 * playerPitch_sin;
        z2 = tmp;
        tmp = x3 * playerYaw_cos - z3 * playerYaw_sin;
        z3 = x3 * playerYaw_sin + z3 * playerYaw_cos;
        x3 = tmp;
        tmp = z3 * playerPitch_cos + y3 * playerPitch_sin;
        y3 = y3 * playerPitch_cos - z3 * playerPitch_sin;
        z3 = tmp;
        tmp = x4 * playerYaw_cos - z4 * playerYaw_sin;
        z4 = x4 * playerYaw_sin + z4 * playerYaw_cos;
        x4 = tmp;
        tmp = z4 * playerPitch_cos + y4 * playerPitch_sin;
        y4 = y4 * playerPitch_cos - z4 * playerPitch_sin;
        z4 = tmp;
        if(outline)
        {
            draw_line(buffer, x1, y1, z1, x2, y2, z2);
            draw_line(buffer, x2, y2, z2, x3, y3, z3);
            draw_line(buffer, x3, y3, z3, x4, y4, z4);
            draw_line(buffer, x4, y4, z4, x1, y1, z1);
        }
        render3(buffer, dsu, x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
        render3(buffer, dsu, x1, y1, z1, x4, y4, z4, x3, y3, z3, color);
    }
    private static void draw_line(int[] buffer, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        double tmp;
        if(z1 > z2)
        {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
            tmp = y1;
            y1 = y2;
            y2 = tmp;
            tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        if(z2 < 1e-6)
            return; // invisible
        else if(z1 < 1e-6)
        {
            tmp = (z2 - 1e-6) / (z2 - z1);
            x1 = x2 - (x2 - x1) * tmp;
            y1 = y2 - (y2 - y1) * tmp;
            z1 = 1e-6;
        }
        x1 = 320 + (x1 / z1) * 250;
        y1 = 240 - (y1 / z1) * 250;
        x2 = 320 + (x2 / z2) * 250;
        y2 = 240 - (y2 / z2) * 250;
        if(x1 == x2 && y1 == y2)
        {
            if(x1 >= 0 && x1 <= 639 && y1 >= 0 && y1 <= 639)
            {
                int x = (int)x1;
                int y = (int)y1;
                if(buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; // black
            }
            return;
        }
        double xdiff = x1 - x2;
        double ydiff = y1 - y2;
        double xd = xdiff<0?-xdiff:xdiff;
        double yd = ydiff<0?-ydiff:ydiff;
        if(xd > yd)
        {
            double xstart = (xdiff<0)?x1:x2;
            double xend = x1+x2-xstart;
            if(xstart < 0)
                xstart = 0;
            if(xend > 639)
                xend = 639;
            int xa = (int)Math.floor(xstart);
            int xb = (int)Math.ceil(xend);
            for(int x = xa; x <= xb; x++)
            {
                int y = (int)(y1 + (y2 - y1) * (x - x1) / (x2 - x1));
                if(y >= 0 && y < 480 && buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; //black
            }
        }
        else
        {
            double ystart = (ydiff<0)?y1:y2;
            double yend = y1+y2-ystart;
            if(ystart < 0)
                ystart = 0;
            if(yend > 479)
                yend = 479;
            int ya = (int)Math.floor(ystart);
            int yb = (int)Math.ceil(yend);
            for(int y = ya; y <= yb; y++)
            {
                int x = (int)(x1 + (x2 - x1) * (y - y1) / (y2 - y1));
                if(x >= 0 && x < 640 && buffer[y*640+x] == 0)
                    buffer[y*640+x] = 0xff000000; //black
            }
        }
    }
    private static void render3(int[] buffer, int[] dsu, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, int color)
    {
        double tmp;
        if(z1 > z2)
        {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
            tmp = y1;
            y1 = y2;
            y2 = tmp;
            tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        if(z1 > z3)
        {
            tmp = x1;
            x1 = x3;
            x3 = tmp;
            tmp = y1;
            y1 = y3;
            y3 = tmp;
            tmp = z1;
            z1 = z3;
            z3 = tmp;
        }
        if(z2 > z3)
        {
            tmp = x2;
            x2 = x3;
            x3 = tmp;
            tmp = y2;
            y2 = y3;
            y3 = tmp;
            tmp = z2;
            z2 = z3;
            z3 = tmp;
        }
        if(z3 < 1e-6)
            return; // invisible to the player
        else if(z2 < 1e-6)
        {
            tmp = (z3 - 1e-6) / (z3 - z2);
            x2 = x3 - (x3 - x2) * tmp;
            y2 = y3 - (y3 - y2) * tmp;
            z2 = 1e-6;
            tmp = (z3 - 1e-6) / (z3 - z1);
            x1 = x3 - (x3 - x1) * tmp;
            y1 = y3 - (y3 - y1) * tmp;
            z1 = 1e-6;
        }
        else if(z1 < 1e-6)
        {
            double x4, y4, z4, x5, y5, z5;
            tmp = (z2 - 1e-6) / (z2 - z1);
            x4 = x2 - (x2 - x1) * tmp;
            y4 = y2 - (y2 - y1) * tmp;
            z4 = 1e-6;
            tmp = (z3 - 1e-6) / (z3 - z1);
            x5 = x3 - (x3 - x1) * tmp;
            y5 = y3 - (y3 - y1) * tmp;
            z5 = 1e-6;
            render3_raw(buffer, dsu, x4, y4, z4, x5, y5, z5, x3, y3, z3, color);
            render3_raw(buffer, dsu, x4, y4, z4, x2, y2, z2, x3, y3, z3, color);
            return;
        }
        render3_raw(buffer, dsu, x1, y1, z1, x2, y2, z2, x3, y3, z3, color);
    }
    private static void render3_raw(int[] buffer, int[] dsu, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, int color)
    {
        double x1_0 = x1;
        double x2_0 = x2;
        double x3_0 = x3;
        double y1_0 = y1;
        double y2_0 = y2;
        double y3_0 = y3;
        double z1_0 = z1;
        double z2_0 = z2;
        double z3_0 = z3;
        x1 = 320 + (x1 / z1) * 250;
        y1 = 240 - (y1 / z1) * 250;
        x2 = 320 + (x2 / z2) * 250;
        y2 = 240 - (y2 / z2) * 250;
        x3 = 320 + (x3 / z3) * 250;
        y3 = 240 - (y3 / z3) * 250;
        double tmp;
        if(y1 > y2)
        {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        if(y1 > y3)
        {
            tmp = x1;
            x1 = x3;
            x3 = tmp;
            tmp = y1;
            y1 = y3;
            y3 = tmp;
        }
        if(y2 > y3)
        {
            tmp = x2;
            x2 = x3;
            x3 = tmp;
            tmp = y2;
            y2 = y3;
            y3 = tmp;
        }
        int starty = (int)Math.floor(y1);
        if(starty < 0)
            starty = 0;
        int endy = (int)Math.ceil(y3);
        if(endy >= 480)
            endy = 479;
        if(y1 == y3)
            return;
        double xa_prev = 0. / 0;
        double xb_prev = 0. / 0;
        for(int y = starty; y <= endy; y++)
        {
            double xa = x1 + (x3 - x1) * (y - y1) / (y3 - y1);
            double xb;
            if(y < y2)
                xb = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
            else
                xb = x2 + (x3 - x2) * (y - y2) / (y3 - y2);
            if(y < y1)
                xa = xb = x1;
            else if(y > y3)
                xa = xb = x3;
            if(xb * 0 != 0)
                xb = x2;
            if(xb < xa)
            {
                tmp = xb;
                xb = xa;
                xa = tmp;
            }
            if(xa < 0)
                xa = 0;
            if(xb > 639)
                xb = 639;
            if(xa > xb)
                continue;
            xa_prev = xa;
            xb_prev = xb;
            int start = 640 * y + (int)Math.floor(xa);
            int end = 640 * y + (int)Math.ceil(xb) + 1;
            int i = start;
            while(i < end)
            {
                if(buffer[i] == 0)
                    buffer[i] = color;
                int next = dsu[i];
                if(next < end)
                    dsu[i] = end;
                i = next;
            }
        }
    }
    private void onkeydown(int key)
    {
        System.out.println("keydown "+key);
        if(key == 65 || key == 412)
            vel_x--;
        if(key == 87 || key == 425)
            vel_z++;
        if(key == 68 || key == 417)
            vel_x++;
        if(key == 83 || key == 424)
            vel_z--;
        /*if(key == 32 || key == 10)
            vel_y++;
        if(key == 16 || key == 461)
            vel_y--;*/
        if(key == 37)
            vel_yaw--;
        if(key == 38)
            vel_pitch++;
        if(key == 39)
            vel_yaw++;
        if(key == 40)
            vel_pitch--;
    }
    private void onkeyup(int key)
    {
        System.out.println("keyup "+key);
        if(key == 65 || key == 412)
            vel_x++;
        if(key == 87 || key == 425)
            vel_z--;
        if(key == 68 || key == 417)
            vel_x--;
        if(key == 83 || key == 424)
            vel_z++;
        /*if(key == 32 || key == 10)
            vel_y--;
        if(key == 16 || key == 461)
            vel_y++;*/
        if(key == 37)
            vel_yaw++;
        if(key == 38)
            vel_pitch--;
        if(key == 39)
            vel_yaw--;
        if(key == 40)
            vel_pitch++;
    }
    public void keyEvent(int key)
    {
        if(key > 0)
        {
            if(!keyStates[key])
                onkeydown(key);
            keyStates[key] = true;
        }
        else
        {
            if(keyStates[-key])
                onkeyup(-key);
            keyStates[-key] = false;
        }
    }
}
