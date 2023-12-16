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
import es.uvigo.esei.dai.hybridserver.servicethread.ServiceThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HybridServer implements AutoCloseable {

	private Thread serverThread;
	private boolean stop;
	private final Configuration configuration;
	private ExecutorService threadPool;

	public HybridServer() {
		this.configuration = new Configuration();
		System.out.println("Starting the server with the default parameters");
	}

	public HybridServer(Configuration configuration){
		this.configuration = configuration;
		System.out.println("Starting the server with the configuration parameters");
	}

	public HybridServer(Properties properties) {

		// Ñapa para poder utilizar los test de la semana 3 y 7
		// Convierte Properties en Configuration

		int httpPort = 8888;
		int numClients = 50;
		String dbURL = "jdbc:mysql://localhost:3306/hstestdb";
		String dbUser = "hsdb";
		String dbPasswd = "hsdbpass";

		try {
			if (!properties.getProperty("numClients").equals("null")){
				numClients = Integer.parseInt(properties.getProperty("numClients"));
			}
			if (!properties.getProperty("port").equals("null")){
				httpPort = Integer.parseInt(properties.getProperty("port"));
			}
			if (!properties.getProperty("db.url").equals("null")){
				dbURL = properties.getProperty("db.url");
			}
			if (!properties.getProperty("db.user").equals("null")){
				dbUser = properties.getProperty("db.user");
			}
			if (!properties.getProperty("db.password").equals("null")){
				dbPasswd = properties.getProperty("db.password");
			}
		} catch (Exception e) {
			System.err.println("An error ocurred loading the configuration parameters");
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(1);
        }

		this.configuration = new Configuration(httpPort, numClients, null, dbUser, dbPasswd, dbURL, new ArrayList<>());
		System.out.println("Starting the server with the configuration parameters");
	}

	public int getPort() {
		return this.configuration.getHttpPort();
	}

	public void start() {
		this.serverThread = new Thread() {
			@Override
			public void run() {
				try (final ServerSocket serverSocket = new ServerSocket(configuration.getHttpPort())) {
					threadPool = Executors.newFixedThreadPool(configuration.getNumClients());
					while (true) {
						Socket clientSocket = serverSocket.accept();
						if (stop)
							break;
						threadPool.execute(new ServiceThread(clientSocket, configuration));
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		};

		this.stop = false;
		this.serverThread.start();
	}

	@Override
	public void close() {
		this.stop = true;

		try (Socket socket = new Socket("localhost", this.configuration.getHttpPort())) {
			// Esta conexión se hace, simplemente, para "despertar" el hilo servidor
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			this.serverThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		this.serverThread = null;
		threadPool.shutdownNow();

		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}