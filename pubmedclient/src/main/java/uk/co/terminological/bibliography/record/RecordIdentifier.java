package uk.co.terminological.bibliography.record;

import java.util.Optional;

public class RecordIdentifier implements RecordReference {

	IdType idType;
	String id;
	
	@Override
	public Optional<String> getIdentifier() {
		return Optional.of(id);
	}

	@Override
	public IdType getIdentifierType() {
		return idType;
	}
	
	public static RecordIdentifier create(RecordReference ref) {
		RecordIdentifier out = new RecordIdentifier();
		out.id = ref.getIdentifier().get();
		out.idType = ref.getIdentifierType();
		return out;
	}
	
	public static RecordIdentifier create(IdType idType, String id) {
		RecordIdentifier out = new RecordIdentifier();
		out.id = id;
		out.idType = idType;
		return out;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((idType == null) ? 0 : idType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordIdentifier other = (RecordIdentifier) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (idType != other.idType)
			return false;
		return true;
	}

}
