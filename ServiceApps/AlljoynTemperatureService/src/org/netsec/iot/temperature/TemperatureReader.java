/*
 * Copyright Rui Tu. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.netsec.iot.temperature;

import java.io.File;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class TemperatureReader {
	private String ds18b20 = "";
		
	public TemperatureReader() {
		init();
	}

	private void init() {
        executeCommand("sudo modprobe w1-gpio");
        executeCommand("sudo modprobe w1-therm");
	}

    private static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";           
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

	public double read() throws FileNotFoundException {
        String prefix = "/sys/bus/w1/devices/";
		String sensorLocation;
		BufferedReader br = null;
		String targetLine;
		File directory = new File(prefix);
		
		double finalTemp = -99999999.0;

		File[] contents = directory.listFiles();
		for (File f: contents) {
			if (!f.getName().equals("w1_bus_master1")) {
				this.ds18b20 = f.getName();
			}
		}

		sensorLocation = prefix + ds18b20 + "/w1_slave";

		try {

			br = new BufferedReader(new FileReader(sensorLocation));
			
			br.readLine();
			targetLine = br.readLine();

			int i = 0;
			while (targetLine.charAt(i) != '=' && i < targetLine.length()) {
				i++;
			}
			
			if (i != targetLine.length()) {
				int readValue = Integer.parseInt( targetLine.substring(i + 1, targetLine.length() - 1) );
				finalTemp = readValue / 100.0;
			}

            br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		

		return finalTemp;
	}
}
