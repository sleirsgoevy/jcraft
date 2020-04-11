import PIL.Image

atlas = PIL.Image.new("RGBA", (256, 256))

locations = open('textures/atlas.txt').read().strip().split('\n')

for y, l in enumerate(locations):
    for x, i in enumerate(l.split(' ')):
        if not i: continue
        img = PIL.Image.open('textures/'+i+'.png')
        assert img.size == (16, 16)
        atlas.paste(img, (x*16, y*16))

atlas.save('textures/atlas.png')
