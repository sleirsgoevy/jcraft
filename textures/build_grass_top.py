import PIL.Image, sys

img = PIL.Image.open('textures/'+sys.argv[1]+'_0.png').convert('RGB')
assert img.size == (16, 16)

for i in range(16):
    for j in range(16):
        r, g, b = img.getpixel((i, j))
        img.putpixel((i, j), (163*r//255, g, 105*r//255))

img.save('textures/'+sys.argv[1]+'.png')
