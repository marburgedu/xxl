/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2011 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library;  If not, see <http://www.gnu.org/licenses/>. 

    http://code.google.com/p/xxl/

*/

package xxl.core.io;

import xxl.core.io.raw.NativeRawAccess;
import xxl.core.io.raw.RAMRawAccess;
import xxl.core.io.raw.RawAccess;
import xxl.core.io.raw.RawAccessUtils;
import xxl.core.util.WrappingRuntimeException;

/**
 * Copies a floppy Disk with NativeRawAccess (buffers the data
 * in a RAMRawAccess while copying).
 */
public class FloppyCopier {

	/**
	 * Waits for a keystroke.
	 */
	private static void presskey() {
		System.out.print("Please press any key");
		try {
			System.in.read();
		} catch (java.io.IOException e) {
			System.out.println("Break");
			throw new WrappingRuntimeException(e);
		}
		System.out.println();
	}

	/**
	 * Main method
	 *
	 * @param args the first parameter has to be the path to the floppy drive
	 *	(for example \\.\a:).
	 */
	public static void main(String args[]) {
		System.out.println("FloppyCopier");
		if (args.length==0) {
			System.out.println("You have to specify the path to the floppy drive!");
			return;
		}
		System.out.println("Copying disc from " + args[0]);

		RawAccess raFloppy = new NativeRawAccess(args[0]);

		long blocks = raFloppy.getNumSectors();
		System.out.println("RAW Sectors: " + blocks);

		RawAccess raRAM = new RAMRawAccess(blocks);
		System.out.println("RAM disk sectors: " + raRAM.getNumSectors());

		System.out.print("Reading disk...");
		RawAccessUtils.copyRawAccess(raFloppy,raRAM);
		raFloppy.close();

		System.out.println("Please insert target disk into " + args[0]);
		presskey();

		System.out.print("Writing disk...");
		raFloppy = new NativeRawAccess(args[0]);
		RawAccessUtils.copyRawAccess(raRAM,raFloppy);

		System.out.println();
		System.out.println("Task completed.");
	}
}
