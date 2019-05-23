package com.inidamleader.querytester.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class ConnectionManager {
	// url examples :
	// jdbc:postgresql:database
	// jdbc:postgresql://host/database
	// jdbc:postgresql://host:port/database
	// jdbc:postgresql://192.168.43.5:5432/Ecole
	// jdbc:hsqldb:file:/home/{user}/{workspace}/Swing/Garage/hsqldb/database/VEHICULE"
	// jdbc:hsqldb:file:/home/reda/workspace/JavaIdeaProjects/Swing/Garage/hsqldb/database/VEHICULE
	private static String sUrl;
	private static String sHost;
	private static String sDatabase;
	private static String sPort;
	private static String sUser;
	private static String sPassword;
	private static String sConnectionConfigFileName;
	private volatile static Connection sConnection;

	public static void setConnectionConfigFileName(String pConnectionConfigFileName) {
		sConnectionConfigFileName = pConnectionConfigFileName;
	}

	public static String getUrl() {
		return sUrl;
	}

	public static void setUrl(String pUrl) {
		sUrl = pUrl;
		sConnection = null;
	}

	public static String getHost() {
		return sHost;
	}

	public static void setHost(String pHost) {
		sHost = pHost;
		sConnection = null;
	}

	public static String getDatabase() {
		return sDatabase;
	}

	public static void setDatabase(String pDatabase) {
		sDatabase = pDatabase;
		sConnection = null;
	}

	public static String getPort() {
		return sPort;
	}

	public static void setPort(String pPort) {
		sPort = pPort;
		sConnection = null;
	}

	public static String getUser() {
		return sUser;
	}

	public static void setUser(String pUser) {
		sUser = pUser;
		sConnection = null;
	}

	public static String getPassword() {
		return sPassword;
	}

	public static void setPassword(String pPassword) {
		sPassword = pPassword;
		sConnection = null;
	}

	public static Connection getConnection() throws SQLException {
		if (sConnection == null)
			synchronized (ConnectionManager.class) {
				if (sConnection == null) {
					System.out.println("Trying initializing Connection");
					String lUrl = sUrl + ":";
					if (sHost.length() != 0) {
						lUrl += "//" + sHost;
						if (sPort.length() != 0)
							lUrl += ":" + sPort;
						lUrl += "/";
					}
					lUrl += sDatabase;
					System.out.println(lUrl);
					sConnection = DriverManager.getConnection(lUrl, sUser, sPassword);
					System.out.println("Connection initialized");
				}
			}
		return sConnection;
	}

	public static void storeParameters() {
		try (FileWriter fw = new FileWriter(sConnectionConfigFileName)) {
			fw.write(sUrl + "\n");
			fw.write(sHost + "\n");
			fw.write(sDatabase + "\n");
			fw.write(sPort + "\n");
			fw.write(sUser + "\n");
			fw.write(sPassword + "\n");
			System.out.println("Connection parameters saved");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadParameters() {
		if (haveParameters()) {
			try (Scanner lScanner = new Scanner(new File(sConnectionConfigFileName))) {
				sUrl = lScanner.nextLine();
				sHost = lScanner.nextLine();
				sDatabase = lScanner.nextLine();
				sPort = lScanner.nextLine();
				sUser = lScanner.nextLine();
				sPassword = lScanner.nextLine();
			} catch (FileNotFoundException pE) {
				pE.printStackTrace();
			}
		}
	}

	public static void deleteParameters() {
		if (haveParameters()) {
			try {
				Files.delete(Paths.get(sConnectionConfigFileName));
				System.out.println("Config connection file deleted");
			} catch (IOException pE) {
				pE.printStackTrace();
			}
		}
	}

	public static boolean haveParameters() {
		return Files.exists(Paths.get(sConnectionConfigFileName));
	}
}