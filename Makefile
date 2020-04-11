JAVAC = javac -source 1.3 -target 1.3 -cp src

src.jar: src/Main.class src/org/homebrew/GameMain.class src/org/homebrew/MapGen.class src/org/homebrew/TextureAtlas.class
	rm -f src.jar
	cd src; zip -r ../src.jar .

src/Main.class: src/Main.java
	$(JAVAC) src/Main.java

src/org/homebrew/GameMain.class: src/org/homebrew/GameMain.java
	$(JAVAC) src/org/homebrew/GameMain.java

src/org/homebrew/MapGen.class: src/org/homebrew/MapGen.java
	$(JAVAC) src/org/homebrew/MapGen.java

src/org/homebrew/TextureAtlas.class: src/org/homebrew/TextureAtlas.java
	$(JAVAC) src/org/homebrew/TextureAtlas.java

src/org/homebrew/TextureAtlas.java: textures/atlas.png textures/gen_java.py
	python3 textures/gen_java.py > src/org/homebrew/TextureAtlas.java

textures/atlas.png: textures/build_atlas.py textures/grass_top.png textures/grass_side.png textures/dirt.png
	python3 textures/build_atlas.py

textures/grass_top.png: textures/build_grass_top.py textures/grass_top_0.png
	python3 textures/build_grass_top.py

textures/grass_top_0.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/grass_top.png > textures/grass_top_0.png

textures/grass_side.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/grass_side.png > textures/grass_side.png

textures/dirt.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/dirt.png > textures/dirt.png

minecraft.jar:
	echo "Put minecraft.jar here to extract the textures!"
	false

clean:
	rm -f src.jar src/*.class src/org/homebrew/*.class src/org/homebrew/TextureAtlas.java textures/*.png
