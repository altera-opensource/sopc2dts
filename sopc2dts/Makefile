
SOURCES=Sopc2DTS.java sopc2dts/Logger.java sopc2dts/generators/AbstractSopcGenerator.java \
	sopc2dts/generators/DTSGenerator.java sopc2dts/generators/KernelHeadersGenerator.java \
	sopc2dts/generators/SopcCreateHeaderFilesImitator.java \
	sopc2dts/lib/BoardInfo.java sopc2dts/lib/SopcComponentLib.java \
	sopc2dts/lib/SopcInfoAssignment.java sopc2dts/lib/SopcInfoConnection.java \
	sopc2dts/lib/SopcInfoElement.java sopc2dts/lib/SopcInfoElementIgnoreAll.java \
	sopc2dts/lib/SopcInfoElementWithParams.java sopc2dts/lib/SopcInfoParameter.java \
	sopc2dts/lib/SopcInfoSystem.java sopc2dts/lib/components/SopcComponentDescription.java \
	sopc2dts/lib/components/SopcInfoComponent.java sopc2dts/lib/components/SopcInfoInterface.java \
	sopc2dts/lib/components/SopcInfoMemoryBlock.java \
	sopc2dts/lib/components/altera/SICEpcs.java \
	sopc2dts/lib/components/altera/SICLan91c111.java \
	sopc2dts/lib/components/altera/SICSgdma.java \
	sopc2dts/lib/components/altera/SICTrippleSpeedEthernet.java \
	sopc2dts/lib/components/base/FlashPartition.java \
	sopc2dts/lib/components/base/SICBridge.java sopc2dts/lib/components/base/SICFlash.java \
	sopc2dts/lib/components/base/SICEthernet.java \
	sopc2dts/lib/components/base/SICUnknown.java

CLASSES=$(SOURCES:.java=.class)

all: sopc2dts.jar

sopc2dts.jar: $(CLASSES) manifest sopc_components_*.xml
	jar -cmf manifest sopc2dts.jar *.java *.class sopc2dts sopc_components_*.xml

%.class: %.java
	javac $<

clean:
	rm -f $(CLASSES)
	rm -f sopc2dts.jar
