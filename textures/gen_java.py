import PIL.Image

img = PIL.Image.open('textures/atlas.png').convert('RGBA')

array = []

for y in range(256):
    for x in range(256):
        r, g, b, a = img.getpixel((x, y))
        if a >= 128: a -= 256
        array.append(a<<24|r<<16|g<<8|b)

while array and not array[-1]: array.pop()

print('package org.homebrew;')
print()
print('public class TextureAtlas')
print('{')
print('    public final static int[] atlas = new int[]{'+repr(array)[1:-1]+'};')
print('}')
