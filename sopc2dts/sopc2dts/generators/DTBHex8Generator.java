package sopc2dts.generators;

import sopc2dts.lib.AvalonSystem;
import sopc2dts.lib.Bin2IHex;
import sopc2dts.lib.Bin2IHex.HexTypes;
import sopc2dts.lib.BoardInfo;

public class DTBHex8Generator extends AbstractSopcGenerator {
	private DTBGenerator dtbGen;
	public DTBHex8Generator(AvalonSystem s) {
		super(s, true);
		dtbGen = new DTBGenerator(s);
	}

	@Override
	public String getExtension() {
		return "hex";
	}

	@Override
	public String getTextOutput(BoardInfo bi) {
		return Bin2IHex.toHex(dtbGen.getBinaryOutput(bi), HexTypes.I8Hex);
	}

}
