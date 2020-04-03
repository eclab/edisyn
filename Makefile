.PHONY: jar install


all: DUMMY
		javac -cp libraries/coremidi4j-1.1.jar:edisyn $$(find edisyn -name '*.java') \

run: DUMMY
	java -cp libraries/coremidi4j-1.1.jar:. edisyn.Edisyn

DUMMY: ;

indent:
	@ echo This uses emacs to indent all of the code.  To indent with
	@ echo "ECJ's default indent style, create a .emacs file in your home"
	@ echo "directory, with the line:    (setq c-default-style \"whitesmith\")"
	@ echo and run make indent.  To indent with BSD/Allman style, use 
	@ echo "the line:    (setq c-default-style \"bsd\")"
	@ echo
	touch ${HOME}/.emacs
	find . -name "*.java" -print -exec emacs --batch --load ~/.emacs --eval='(progn (find-file "{}") (mark-whole-buffer) (setq indent-tabs-mode nil) (untabify (point-min) (point-max)) (indent-region (point-min) (point-max) nil) (save-buffer))' \;

jar:
	rm -rf install/edisyn.jar jar/edisyn.jar uk META-INF
	javac edisyn/*.java edisyn/*/*.java edisyn/*/*/*.java 
	touch /tmp/manifest.add
	rm /tmp/manifest.add
	echo "Main-Class: edisyn.Edisyn" > /tmp/manifest.add
	cd libraries ; jar -xvf coremidi4j-1.1.jar
	mv libraries/META-INF . ; mv libraries/uk .
	jar -cvfm jar/edisyn.jar /tmp/manifest.add edisyn/synth/Synths.txt `find edisyn -name "*.class"` `find edisyn -name "*.init"` `find edisyn -name "*.html"` `find edisyn -name "*.png"` `find edisyn -name "*.jpg"` uk/ META-INF/
	rm -rf uk META-INF

install: jar
	rm -rf install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp
	javapackager -deploy -native dmg -srcfiles jar/edisyn.jar -appclass edisyn.Edisyn -name Edisyn -outdir install -outfile Edisyn.dmg -v
	mv install/bundles/Edisyn-1.0.dmg install/Edisyn.dmg
	rm -rf install/edisyn.jar install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp 

