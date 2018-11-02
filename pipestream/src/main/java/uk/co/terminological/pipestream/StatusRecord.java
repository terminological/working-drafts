package uk.co.terminological.pipestream;

import uk.co.terminological.datatypes.EavMap;

public class StatusRecord<STATUSES> extends EavMap<String,STATUSES,Boolean> {

	public Boolean has(STATUSES status, String key) {
		Boolean tmp = get(key,status);
		return tmp != null ? tmp : Boolean.FALSE;
	}
}
