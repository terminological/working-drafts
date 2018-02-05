package uk.co.terminological.streams.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.similarity.JaroWinklerDistance;

public class NameMapGenerator {

	public static void main(String[] args) throws IOException {
		// Create a variable for the connection string.  
		String connectionUrl = "jdbc:sqlserver://10.174.129.118:1433;" +  
				"databaseName=RobsDatabase;user=RobertSQL"; //;password=XXXXX";  

		// Declare the JDBC objects.  
		Connection con = null;  
		Statement stmt = null;
		ResultSet rs = null;

		Path out = Paths.get("/home/robchallen/clustering3.txt");
		Files.deleteIfExists(out);
		PrintWriter dump = new PrintWriter(Files.newBufferedWriter(out));

		BufferedReader br = new BufferedReader(new InputStreamReader(NameMapGenerator.class.getResourceAsStream("/stopwords.txt")));
		String line;
		ArrayList<String> stopWords = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			stopWords.add(line);
		}

		try {  
			// Establish the connection.  
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
			con = DriverManager.getConnection(connectionUrl);
			stmt = con.createStatement();

			List<User> users = new ArrayList<>();
			List<Clinician> clinicians = new ArrayList<>();

			{
				// Create and execute an SQL statement that returns some data.  
				String SQL = "SELECT user_id,long_name,login,email_address FROM tmpRvUser";  
				rs = stmt.executeQuery(SQL);

				// Iterate through the data in the result set and display it.  
				while (rs.next()) {
					User user = new User();
					user.user_id = rs.getInt(1);
					user.long_name = rs.getString(2);
					user.login = rs.getString(3);
					user.email_address = rs.getString(4);
					users.add(user);
				}
				if (rs != null) try { rs.close(); } catch(Exception e) {}
			}

			{
				// Create and execute an SQL statement that returns some data.  
				String SQL = "SELECT clinician_id, name, name FROM tmpClinician";  

				rs = stmt.executeQuery(SQL);
				// Iterate through the data in the result set and display it.  
				while (rs.next()) {
					Clinician clinician = new Clinician();
					clinician.clinician_id = rs.getInt(1);
					clinician.name = rs.getString(2);
					clinician.code = rs.getString(3);
					clinicians.add(clinician);
				}
				if (rs != null) try { rs.close(); } catch(Exception e) {}
			}
			//scm.getSource().streamDocuments().forEach(dump::println);
			//Stream.concat(
			//		scm.getTarget().streamTerms()
			//		, scm.getTarget().streamTerms())
			//	.sorted(
			//			(t1,t2) -> t2.count - t1.count
			//		).forEach(t -> dump.println(t.tag));



			
			/*scm.getAllMatchesBySignificance(50D).entrySet().stream().forEach(e ->
				{ 
					System.out.println(e.getKey());
					e.getValue().entrySet().stream().forEach(kv -> {
							System.out.println("\t"+kv.getKey()+"\t"+kv.getValue());
							dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
					});
				});*/
			

			{
				StringCrossMapper scm = new StringCrossMapper(stopWords.toArray(new String[] {}));
				users.stream().forEach(u -> scm.addSource(u.long_name));
				clinicians.stream().forEach(c -> scm.addTarget(c.name));

				scm.getAllMatchesByDistance(0.95, new JaroWinklerDistance()).entrySet().stream().forEach(e ->
				{ 
					System.out.println(e.getKey());
					e.getValue().entrySet().stream().forEach(kv -> {
						System.out.println("\t"+kv.getKey()+"\t"+kv.getValue());
						dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
						//dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
					});
				});
			}
			
			{
				StringCrossMapper scm = new StringCrossMapper(stopWords.toArray(new String[] {}));
				users.stream().forEach(u -> scm.addTarget(u.long_name));
				clinicians.stream().forEach(c -> scm.addSource(c.name));

				scm.getAllMatchesByDistance(0.95, new JaroWinklerDistance()).entrySet().stream().forEach(e ->
				{ 
					System.out.println(e.getKey());
					e.getValue().entrySet().stream().forEach(kv -> {
						System.out.println("\t"+kv.getKey()+"\t"+kv.getValue());
						dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
						//dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
					});
				});
			}
			
			{
				StringCrossMapper scm = new StringCrossMapper(stopWords.toArray(new String[] {}));
				users.stream().forEach(u -> scm.addTarget(u.long_name));
				users.stream().forEach(c -> scm.addSource(c.long_name));

				scm.getAllMatchesByDistance(0.95, new JaroWinklerDistance()).entrySet().stream().forEach(e ->
				{ 
					System.out.println(e.getKey());
					e.getValue().entrySet().stream().forEach(kv -> {
						System.out.println("\t"+kv.getKey()+"\t"+kv.getValue());
						dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
						//dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
					});
				});
			}
			
			{
				StringCrossMapper scm = new StringCrossMapper(stopWords.toArray(new String[] {}));
				clinicians.stream().forEach(u -> scm.addTarget(u.name));
				clinicians.stream().forEach(c -> scm.addSource(c.name));

				scm.getAllMatchesByDistance(0.95, new JaroWinklerDistance()).entrySet().stream().forEach(e ->
				{ 
					System.out.println(e.getKey());
					e.getValue().entrySet().stream().forEach(kv -> {
						System.out.println("\t"+kv.getKey()+"\t"+kv.getValue());
						dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
						//dump.println(e.getKey()+"\t"+kv.getKey()+"\t"+kv.getValue());
					});
				});
			}
		}  

		// Handle any errors that may have occurred.  
		catch (Exception e) {  
			e.printStackTrace();  
		}  
		finally {  
			if (rs != null) try { rs.close(); } catch(Exception e) {}  
			if (stmt != null) try { stmt.close(); } catch(Exception e) {}  
			if (con != null) try { con.close(); } catch(Exception e) {}
			dump.close();
		}  
	}  




}