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

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.configuration.XMLConfigurationLoader;

import java.io.FileReader;

public class Launcher {

	public static void main(String[] args) {

//-------------------------------------------------------------------------------------------------------------------
//		Launcher +1 servidor

//		HybridServer[] servers = null;
//
//		if(args.length == 0){
//			servers = new HybridServer[1];
//			servers[0] = new HybridServer();
//		}else{
//			servers = new HybridServer[args.length];
//			Configuration configuration;
//			for (int i = 0; i < args.length; i++){
//				try {
//					configuration = new XMLConfigurationLoader().load(new FileReader(args[i]));
//					servers[i] = new HybridServer(configuration);
//				} catch (Exception e) {
//					System.err.println("An error ocurred reading the configuration parameters number "+ i);
//					System.out.println("Configuration Parameters should be: ");
//					System.out.println("configuration1.xml ... configurationN.xml or nothing for default init");
//					e.printStackTrace();
//					System.err.println("Exiting...");
//					System.exit(1);
//				}
//			}
//		}
//
//		for (HybridServer server : servers){
//			server.start();
//		}


//-------------------------------------------------------------------------------------------------------------------
//		Launcher 1 servidor

		HybridServer server = null;

		switch (args.length) {
			case 0:
				server = new HybridServer();
				break;

			case 1:
				try {
					Configuration configuration = new XMLConfigurationLoader().load(new FileReader(args[0]));
					server = new HybridServer(configuration);
				} catch (Exception e) {
					System.err.println("An error ocurred reading the configuration parameters");
					e.printStackTrace();
					System.err.println("Exiting...");
					System.exit(1);
				}
				break;

			default:
				System.err.println("Wrong Parameters: configuration.xml required or nothing for default init");
				System.err.println("Exiting...");
				System.exit(1);
		}
		server.start();

	}
}