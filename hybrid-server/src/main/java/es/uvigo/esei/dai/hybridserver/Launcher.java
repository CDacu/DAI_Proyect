/**
 *  HybridServer
 *  Copyright (C) 2023 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver;

import java.io.*;
import java.util.Properties;

public class Launcher {

	public static void main(String[] args) {

		HybridServer server = null;

		if(args.length == 0){

			server = new HybridServer();

		}else{
			if(args.length == 1){
				Properties properties = new Properties();

				try(FileInputStream fileInputStream = new FileInputStream(args[0])) {
					properties.load(fileInputStream);
				} catch (FileNotFoundException e) {
					System.err.println("An error ocurred, file doesn't exist");
					System.err.println("Exiting...");
					System.exit(1);
				} catch (IOException e) {
					System.err.println("An error ocurred reading the configuration parameters");
					e.printStackTrace();
					System.err.println("Exiting...");
					System.exit(1);
				}

				server = new HybridServer(properties);

			}else {
				System.err.println("Wrong Parameters: config.conf required or nothing for default init");
				System.err.println("Exiting...");
				System.exit(1);
			}
		}
		server.start();
	}
}