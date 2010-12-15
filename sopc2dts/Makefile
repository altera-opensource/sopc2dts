ll: sopc2dts.jar

sopc2dts.jar: Sopc2DTS.class manifest
	jar -cmf manifest sopc2dts.jar *.java *.class sopc2dts

Sopc2DTS.class: *.java
	javac Sopc2DTS.java

clean:
	rm -f *.class
	rm -f sopc2dts.jar


