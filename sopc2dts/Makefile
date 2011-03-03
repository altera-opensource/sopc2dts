
SOURCES=Sopc2DTS.java sopc2dts/Logger.java sopc2dts/generators/AbstractSopcGenerator.java \
	sopc2dts/generators/DTSGenerator.java sopc2dts/generators/KernelHeadersGenerator.java \
	sopc2dts/generators/SopcCreateHeaderFilesImitator.java \
	sopc2dts/lib/BoardInfo.java sopc2dts/lib/SopcComponentLib.java \
	sopc2dts/lib/AvalonSystem.java sopc2dts/lib/BasicElement.java \
	sopc2dts/lib/Connection.java sopc2dts/lib/Parameter.java \
	sopc2dts/lib/components/BasicComponent.java \
	sopc2dts/lib/components/Interface.java \
	sopc2dts/lib/components/MemoryBlock.java \
	sopc2dts/lib/components/SopcComponentDescription.java \
	sopc2dts/lib/components/altera/SICEpcs.java \
	sopc2dts/lib/components/altera/SICLan91c111.java \
	sopc2dts/lib/components/altera/SICSgdma.java \
	sopc2dts/lib/components/altera/SICTrippleSpeedEthernet.java \
	sopc2dts/lib/components/base/FlashPartition.java \
	sopc2dts/lib/components/base/SCDQSys.java \
	sopc2dts/lib/components/base/SICBridge.java sopc2dts/lib/components/base/SICFlash.java \
	sopc2dts/lib/components/base/SICEthernet.java \
	sopc2dts/lib/components/base/SICUnknown.java \
	sopc2dts/parsers/BasicSystemLoader.java \
	sopc2dts/parsers/qsys/QSysSubSystem.java \
	sopc2dts/parsers/qsys/QSysSystemLoader.java \
	sopc2dts/parsers/sopcinfo/SopcInfoAssignment.java \
	sopc2dts/parsers/sopcinfo/SopcInfoComponent.java \
	sopc2dts/parsers/sopcinfo/SopcInfoConnection.java \
	sopc2dts/parsers/sopcinfo/SopcInfoElement.java \
	sopc2dts/parsers/sopcinfo/SopcInfoElementIgnoreAll.java \
	sopc2dts/parsers/sopcinfo/SopcInfoElementWithParams.java \
	sopc2dts/parsers/sopcinfo/SopcInfoInterface.java \
	sopc2dts/parsers/sopcinfo/SopcInfoParameter.java \
	sopc2dts/parsers/sopcinfo/SopcInfoSystemLoader.java 


CLASSES=$(SOURCES:.java=.class)

all: sopc2dts.jar

sopc2dts.jar: $(CLASSES) manifest sopc_components_*.xml
	jar -cmf manifest sopc2dts.jar *.java *.class sopc2dts sopc_components_*.xml

%.class: %.java
	javac $<

clean:
	rm -f $(CLASSES)
	rm -f sopc2dts.jar
