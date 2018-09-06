package uk.co.terminological.streams.data;

public class Mapping {
	User user;
	Clinician clinician;
	Double similarity;
	
	public String toString() {return user.user_id+"\t"+user.long_name+"\t"+similarity+"\t"+clinician.clinician_id+"\t"+clinician.name;}
}
