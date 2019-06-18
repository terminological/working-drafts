package uk.co.terminological.ctakes;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class CtakesProperties extends Properties {

	private Path USER_HOME = Paths.get(System.getProperty("user.home"));
	private Path WORKING_DIR = Paths.get(System.getProperty("user.dir"));

	private Path DEFAULT_PROP = Paths.get("ctakes.prop");

	public static final int MAX_RETRIES = 3;

	public CtakesProperties(Path... paths) {
		super();
		if (paths.length == 0) paths = new Path[] {DEFAULT_PROP};

		for (Path path: paths) {
			try {

				if (path.isAbsolute()) {
					this.load(Files.newInputStream(path));
				} else {
					if (Files.exists(WORKING_DIR.resolve(path))) {
						this.load(Files.newInputStream(WORKING_DIR.resolve(path)));
					} else if (Files.exists(USER_HOME.resolve(path))) {
						this.load(Files.newInputStream(USER_HOME.resolve(path)));
					} else {
						throw new RuntimeException("could not load property file: "+path+" from "+USER_HOME+" or "+WORKING_DIR);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("could not load property file: "+path+" from "+USER_HOME+" or "+WORKING_DIR);
			}
		}
		
		System.setProperty("logfile.name",logLocation());
		URL log = CtakesProperties.class.getClassLoader().getResource("log4j.properties");
		PropertyConfigurator.configure(log);

	}
	
	public String logLocation() {
		String tmp = this.getProperty("logfile.name", "ctakes.log");
		if (Paths.get(tmp).isAbsolute()) return tmp;
		return ctakesHome().resolve(tmp).toAbsolutePath().toString();
	} 
	
	private Path ctakesHome(String nlpSystemVersion) {
		if (this.containsKey("ctakes.resources")) {
			String tmp = this.getProperty("ctakes.resources");
			if (Paths.get(tmp).isAbsolute()) {
				return Paths.get(tmp).resolve(nlpSystemVersion);
			} else {
				return USER_HOME.resolve(tmp).resolve(nlpSystemVersion);
			}
		} else {
			return WORKING_DIR.resolve(nlpSystemVersion);
		}
	}

	public String nlpSystemVersion() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

	public String nlpSystem() {
		return "CTAKESv1";
		//TODO:
	}

	public Path ctakesHome() {
		return ctakesHome(nlpSystemVersion());
	}

	protected String umlsUser() {
		return this.getProperty("umls.user");
	}

	protected String umlsPw() {
		return this.getProperty("umls.pw");
	}

	/**
	 * normally a driver, url, user, password properties will be provided in the property file or minimally a driver, url
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection dbConnection() throws ClassNotFoundException, SQLException {
		Class.forName(this.getProperty("driver"));
		return DriverManager.getConnection(this.getProperty("url"), this);
	}

}
