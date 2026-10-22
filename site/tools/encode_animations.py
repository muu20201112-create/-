from PIL import Image
from pathlib import Path
r=Path(__file__).resolve().parents[1]/'assets'
for name,frames in [('opening',['intro','motion-0']),('scenes',['motion-5','motion-4','motion-0'])]:
 images=[Image.open(r/(f+'.png')).convert('RGB') for f in frames]
 assert len({im.size for im in images})==1
 images[0].save(r/(name+'.gif'),save_all=True,append_images=images[1:],duration=2400,loop=0,optimize=True)
 print(name,'frames',len(images))
