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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HybridServer implements AutoCloseable {
	private final int NUMCLIENTS = 50;
	private final int HTTPPORT = 8888;
	private final String DBURL = "jdbc:mysql://localhost:3306/hstestdb";
	private final String DBUSER = "hsdb";
	private final String DBPASSWD = "hsdbpass";
	private Thread serverThread;
	private boolean stop;
	private HTMLDao pages;
	private int numClients, httpPort;
	private String dbURL, dbUser, dbPasswd;
	private ExecutorService threadPool;

	public HybridServer() {

		this.numClients = NUMCLIENTS;
		this.httpPort = HTTPPORT;
		this.dbURL = DBURL;
		this.dbUser = DBUSER;
		this.dbPasswd = DBPASSWD;

		System.out.println("Starting the server with the default parameters");

		this.pages = new HTMLDaoDB(dbURL, dbUser, dbPasswd);
	}

	public HybridServer(Map<String, String> pages) {

		this.numClients = NUMCLIENTS;
		this.httpPort = HTTPPORT;
		this.pages = new HTMLDaoMap(pages);
	}

	public HybridServer(Properties properties) {

		try {
			if (!properties.getProperty("numClients").equals("null")){
				this.numClients = Integer.parseInt(properties.getProperty("numClients"));
			}else{
				this.numClients = NUMCLIENTS;
			}
			if (!properties.getProperty("port").equals("null")){
				this.httpPort = Integer.parseInt(properties.getProperty("port"));
			}else{
				this.httpPort = HTTPPORT;
			}
			if (!properties.getProperty("db.url").equals("null")){
				this.dbURL = properties.getProperty("db.url");
			}else{
				this.dbURL = DBURL;
			}
			if (!properties.getProperty("db.user").equals("null")){
				this.dbUser = properties.getProperty("db.user");
			}else{
				this.dbUser = DBUSER;
			}
			if (!properties.getProperty("db.password").equals("null")){
				this.dbPasswd = properties.getProperty("db.password");
			}else{
				this.dbPasswd = DBPASSWD;
			}

		} catch (Exception e) {
			System.err.println("An error ocurred loading the configuration parameters");
			e.printStackTrace();
			System.err.println("Exiting...");
			System.exit(1);
        }

		System.out.println("Starting the server with the configuration parameters");

		this.pages = new HTMLDaoDB(dbURL, dbUser, dbPasswd);
	}

	public int getPort() {
		return this.httpPort;
	}

	public void start() {

		this.serverThread = new Thread() {
			@Override
			public void run() {

				try (final ServerSocket serverSocket = new ServerSocket(httpPort)) {

					threadPool = Executors.newFixedThreadPool(numClients);

					while (true) {

						Socket clientSocket = serverSocket.accept();

						if (stop)
							break;

						threadPool.execute(new ServiceThread(clientSocket, pages));
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

		try (Socket socket = new Socket("localhost", this.httpPort)) {
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