import PIL.Image, sys

x = PIL.Image.open('textures/'+sys.argv[1]+'_0.png')
x.crop((0, 0, 16, 16)).save('textures/'+sys.argv[1]+'.png')
