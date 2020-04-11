import PIL.Image

img = PIL.Image.open('textures/grass_top_0.png').convert('RGB')
assert img.size == (16, 16)

for i in range(16):
    for j in range(16):
        r, g, b = img.getpixel((i, j))
        img.putpixel((i, j), (163*r//255, g, 105*r//255))

img.save('textures/grass_top.png')
