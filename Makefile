.PHONY: jar install

JAVAC = javac ${JAVACFLAGS}
JACKLIBS = libraries/jack-midi-spi-1.0.0.jar:libraries/jnajack-1.5.1.jar:libraries/jna-5.13.0.jar

all:
	${JAVAC} -cp libraries/coremidi4j-1.6.jar:${JACKLIBS}:edisyn $$(find edisyn -name '*.java')

run:
	java -cp libraries/coremidi4j-1.6.jar:${JACKLIBS}:. edisyn.Edisyn

indent:
	touch ${HOME}/.emacs
	find . -name "*.java" -print -exec emacs --batch --load ~/.emacs --eval='(progn (find-file "{}") (mark-whole-buffer) (setq indent-tabs-mode nil) (untabify (point-min) (point-max)) (indent-region (point-min) (point-max) nil) (save-buffer))' \;

jar:
	- mkdir install
	rm -rf install/edisyn.jar uk org com casa META-INF _jarstage
	${JAVAC} -cp libraries/coremidi4j-1.6.jar:${JACKLIBS}:. edisyn/*.java edisyn/*/*.java edisyn/*/*/*.java
	touch /tmp/manifest.add
	rm /tmp/manifest.add
	echo "Main-Class: edisyn.Edisyn" > /tmp/manifest.add
	mkdir -p _jarstage/coremidi4j _jarstage/jackspi _jarstage/jnajack _jarstage/jna _jarstage/merged/META-INF/services
	cd _jarstage/coremidi4j ; jar -xf ../../libraries/coremidi4j-1.6.jar
	cd _jarstage/jackspi ; jar -xf ../../libraries/jack-midi-spi-1.0.0.jar
	cd _jarstage/jnajack ; jar -xf ../../libraries/jnajack-1.5.1.jar
	cd _jarstage/jna ; jar -xf ../../libraries/jna-5.13.0.jar
	cp -r _jarstage/coremidi4j/uk _jarstage/jackspi/casa _jarstage/jnajack/org _jarstage/jna/com _jarstage/merged/
	cat _jarstage/coremidi4j/META-INF/services/javax.sound.midi.spi.MidiDeviceProvider _jarstage/jackspi/META-INF/services/javax.sound.midi.spi.MidiDeviceProvider > _jarstage/merged/META-INF/services/javax.sound.midi.spi.MidiDeviceProvider
	mv _jarstage/merged/uk _jarstage/merged/org _jarstage/merged/com _jarstage/merged/casa _jarstage/merged/META-INF .
	jar -cvfm install/edisyn.jar /tmp/manifest.add edisyn/synth/synths.txt edisyn/gui/wordlist.txt edisyn/Manufacturers.txt `find edisyn -name "*.class"` `find edisyn -name "*.init"` `find edisyn -name "*.html"` `find edisyn -name "*.png"` `find edisyn -name "*.jpg"` `find edisyn/synth/ -name "*.txt.gz"` `find edisyn/synth/ -name "n_*.txt"` edisyn/synth/kawaik5000/kharmonics.out uk/ org/ com/ casa/ META-INF/
	echo jar -cvfm install/edisyn.jar /tmp/manifest.add edisyn/synth/synths.txt edisyn/gui/wordlist.txt edisyn/Manufacturers.txt `find edisyn -name "*.class"` `find edisyn -name "*.init"` `find edisyn -name "*.html"` `find edisyn -name "*.png"` `find edisyn -name "*.jpg"` `find edisyn/synth/ -name "*.txt.gz"` `find edisyn/synth/ -name "n_*.txt"` edisyn/synth/kawaik5000/kharmonics.out uk/ org/ com/ casa/ META-INF/
	rm -rf uk org com casa META-INF _jarstage

install: clean jar
	rm -rf app/Edisyn.app install/jar install/Edisyn.app install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp
	mkdir install/jar
	cp install/edisyn.jar install/jar/
	# See https://alvinalexander.com/java/how-use-jpackage-command-java-14-jdk-sdk/
	- jpackage --type dmg --verbose --input install/jar --dest app --name Edisyn --main-jar edisyn.jar --main-class edisyn.Edisyn
	# - mv install/bundles/Edisyn-1.0.dmg install/Edisyn.dmg
	rm -rf install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp install/jar

installold: clean jar
	rm -rf app/Edisyn.app install/jar install/Edisyn.app install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp
	mkdir install/jar
	cp install/edisyn.jar install/jar/
	- javapackager -deploy -native dmg -srcfiles install/edisyn.jar -appclass edisyn.Edisyn -name Edisyn -outdir install -outfile Edisyn.dmg -v
	rm -rf install/bundles install/Edisyn.dmg.html install/Edisyn.dmg.jnlp install/jar



clean:
	find . -name "*.class" -exec rm -f {} \;
	find . -name ".DS_Store" -exec rm -f {} \;
	find . -name "*.java*~" -exec rm -f {} \;
	find . -name ".#*" -exec rm -rf {} \;
	find . -name "#*#" -exec rm -rf {} \;
