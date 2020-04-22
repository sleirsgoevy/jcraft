JAVAC = javac -source 1.3 -target 1.3 -cp src
GEN_SRC = src/org/homebrew/TextureAtlas.java src/org/homebrew/FontBundle.java

src.jar: src/Main.class src/org/homebrew/GameMain.class src/org/homebrew/MapGen.class src/org/homebrew/FontRenderer.class src/org/homebrew/Inventory.class src/org/homebrew/ItemNames.java src/org/homebrew/GUI.class src/org/homebrew/GUIWithButtons.class src/org/homebrew/StartMenu.class src/org/homebrew/PauseMenu.class src/org/homebrew/LevelSave.class $(GEN_SRC)
	rm -f src.jar
	cd src; zip -r ../src.jar .

src/Main.class: src/Main.java $(GEN_SRC)
	$(JAVAC) src/Main.java

src/org/homebrew/GameMain.class: src/org/homebrew/GameMain.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/GameMain.java

src/org/homebrew/MapGen.class: src/org/homebrew/MapGen.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/MapGen.java

src/org/homebrew/FontRenderer.class: src/org/homebrew/FontRenderer.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/FontRenderer.java

src/org/homebrew/Inventory.class: src/org/homebrew/Inventory.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/Inventory.java

src/org/homebrew/ItemNames.class: src/org/homebrew/ItemNames.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/ItemNames.java

src/org/homebrew/GUI.class: src/org/homebrew/GUI.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/GUI.java

src/org/homebrew/GUIWithButtons.class: src/org/homebrew/GUIWithButtons.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/GUIWithButtons.java

src/org/homebrew/StartMenu.class: src/org/homebrew/StartMenu.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/StartMenu.java

src/org/homebrew/PauseMenu.class: src/org/homebrew/PauseMenu.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/PauseMenu.java

src/org/homebrew/LevelSave.class: src/org/homebrew/LevelSave.java $(GEN_SRC)
	$(JAVAC) src/org/homebrew/LevelSave.java

src/org/homebrew/TextureAtlas.class: src/org/homebrew/TextureAtlas.java
	$(JAVAC) src/org/homebrew/TextureAtlas.java

src/org/homebrew/TextureAtlas.java: textures/atlas.png textures/gen_java.py
	python3 textures/gen_java.py > src/org/homebrew/TextureAtlas.java

src/org/homebrew/FontBundle.class: src/org/homebrew/FontBundle.java
	$(JAVAC) src/org/homebrew/FontBundle.java

src/org/homebrew/FontBundle.java: dump_font.py bios.bin
	python3 dump_font.py > src/org/homebrew/FontBundle.java

textures/atlas.png: textures/build_atlas.py textures/atlas.txt textures/grass_top.png textures/grass_side.png textures/dirt.png textures/log_oak.png textures/log_oak_top.png textures/leaves_oak.png
	python3 textures/build_atlas.py

textures/grass_top.png: textures/build_grass_top.py textures/grass_top_0.png
	python3 textures/build_grass_top.py grass_top

textures/grass_top_0.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/grass_top.png > textures/grass_top_0.png

textures/leaves_oak.png: textures/build_grass_top.py textures/leaves_oak_0.png
	python3 textures/build_grass_top.py leaves_oak

textures/leaves_oak_0.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/leaves_oak.png > textures/leaves_oak_0.png

textures/grass_side.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/grass_side.png > textures/grass_side.png

textures/dirt.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/dirt.png > textures/dirt.png

textures/log_oak.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/log_oak.png > textures/log_oak.png

textures/log_oak_top.png: minecraft.jar
	unzip -p minecraft.jar assets/minecraft/textures/blocks/log_oak_top.png > textures/log_oak_top.png

minecraft.jar:
	echo "Put minecraft.jar here to extract the textures!"
	false

clean:
	rm -f src.jar src/*.class src/org/homebrew/*.class $(GEN_SRC) textures/*.png
