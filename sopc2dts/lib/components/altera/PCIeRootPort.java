/*
sopc2dts - Devicetree generation for Altera systems

Copyright (C) 2016 Tien Hock Loh <thloh@altera.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package sopc2dts.lib.components.altera;

import java.util.ArrayList;
import java.util.Vector;

import sopc2dts.lib.BoardInfo;
import sopc2dts.lib.Connection;
import sopc2dts.lib.Parameter;
import sopc2dts.lib.components.BasicComponent;
import sopc2dts.lib.components.Interface;
import sopc2dts.lib.components.SopcComponentDescription;
import sopc2dts.lib.devicetree.DTNode;
import sopc2dts.lib.devicetree.DTPropPHandleVal;
import sopc2dts.lib.devicetree.DTPropStringVal;
import sopc2dts.lib.devicetree.DTProperty;

public class PCIeRootPort extends BasicComponent {

	public static final long PCI_PHYS_HI_RELOCATABLE 	= (1<<31);
	public static final long PCI_PHYS_HI_PREFETCHABLE 	= (1<<30);
	public static final long PCI_PHYS_HI_ALIASED 		= (1<<29);
	public static final long PCI_PHYS_HI_SPACE_CONFIG 	= (0<<24);
	public static final long PCI_PHYS_HI_SPACE_IO 		= (1<<24);
	public static final long PCI_PHYS_HI_SPACE_MEM32 	= (2<<24);
	public static final long PCI_PHYS_HI_SPACE_MEM64 	= (3<<24);

	public PCIeRootPort(String cName, String iName, String ver,
			SopcComponentDescription scd) {
		super(cName, iName, ver, scd);
	}

	public PCIeRootPort(BasicComponent comp) {
		super(comp);
	}

	private void updateTxsNode(DTNode node) {
		DTProperty property = node.getPropertyByName("reg-names");
		if(property == null) {
			return;
		}
		for(int i=0; i<property.getValues().size(); i++) {
			if(property.getValues().get(i).toString().equals("\"txs\""))
				property.getValues().set(i, new DTPropStringVal("Txs"));
			if(property.getValues().get(i).toString().equals("\"cra\""))
				property.getValues().set(i, new DTPropStringVal("Cra"));
		}
	}

	private BasicComponent findMSIController(Vector<Interface> vector) {
		for (Interface i : vector) {
			if(interfaceshandled.contains(i))
				continue;
			for (Connection c : i.getConnections()) {
				Parameter param = c.getSlaveInterface().getOwner().getParamByName("embeddedsw.dts.params.msi-controller");
				if(param != null) {
					return c.getSlaveInterface().getOwner();
				}
				interfaceshandled.add(i);
				BasicComponent component = findMSIController(c.getSlaveModule().getInterfaces());
				if(component != null)
					return component;
			}
		}

		return null;
	}

	private void addTxsNode(Connection conn, DTNode node) {
		Vector<String> vRegNames = new Vector<String>();
		Vector<Long> vRegs = getReg((conn != null ? conn.getMasterModule() : null), vRegNames);
		int txsRegPosition;
		int width = 2;
		if (conn != null)
			width = conn.getMasterInterface().getPrimaryWidth() + conn.getMasterInterface().getSecondaryWidth();

		for(txsRegPosition=0; txsRegPosition<vRegNames.size(); txsRegPosition++) {
			if(vRegNames.get(txsRegPosition).equalsIgnoreCase("txs"))
				break;
		}

		if(vRegs.size() > 0) {
			node.addProperty(new DTProperty("ranges"));
			node.getPropertyByName("ranges").addHexValues(
					new long[] {PCI_PHYS_HI_RELOCATABLE | PCI_PHYS_HI_SPACE_MEM32, 0, 0,
							vRegs.get(txsRegPosition*width), vRegs.get(txsRegPosition*width + 1),
							0x0, vRegs.get(txsRegPosition*width + 2)});
		}
	}

	@Override
	public DTNode toDTNode(BoardInfo bi, Connection conn) {
		DTNode node = super.toDTNode(bi, conn);
		node.addProperty(new DTProperty("bus-range"));

		interfaceshandled.clear();

		node.getPropertyByName("bus-range").addHexValues(new long[]{0, 0xFF});

		addTxsNode(conn, node);
		updateTxsNode(node);

		BasicComponent msi_controller = findMSIController(this.vInterfaces);
		if(msi_controller != null) {
			DTProperty msi_dtprop = new DTProperty("msi-parent");
			msi_dtprop.addValue(new DTPropPHandleVal(msi_controller));
			node.addProperty(msi_dtprop);
		}

		node.addProperty(new DTProperty("#address-cells", 3));
		node.addProperty(new DTProperty("#size-cells", 2));
		node.addProperty(new DTProperty("interrupt-map-mask"));
		node.addProperty(new DTProperty("interrupt-map"));
		node.getPropertyByName("interrupt-map-mask").addNumberValues(new long[]{0, 0, 0, 7});

		node.getPropertyByName("interrupt-map").setNumValuesPerRow(6);
		for (int i=1; i<5; i++) {
			node.getPropertyByName("interrupt-map").addNumberValues(new long[] {0, 0, 0, i});
			node.getPropertyByName("interrupt-map").addValue(new DTPropPHandleVal(this));
			node.getPropertyByName("interrupt-map").addNumberValues(new long[] {i});
		}
		return node;
	}

	ArrayList<Interface> interfaceshandled = new ArrayList<Interface>();
}
