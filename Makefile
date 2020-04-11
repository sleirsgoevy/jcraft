JAVAC = javac -source 1.3 -target 1.3 -cp src

src.jar: src/Main.class src/org/homebrew/GameMain.class src/org/homebrew/MapGen.class
	rm -f src.jar
	cd src; zip -r ../src.jar .

src/Main.class: src/Main.java
	$(JAVAC) src/Main.java

src/org/homebrew/GameMain.class: src/org/homebrew/GameMain.java
	$(JAVAC) src/org/homebrew/GameMain.java

src/org/homebrew/MapGen.class: src/org/homebrew/MapGen.java
	$(JAVAC) src/org/homebrew/MapGen.java

clean:
	rm src.jar src/*.class src/org/homebrew/*.class
