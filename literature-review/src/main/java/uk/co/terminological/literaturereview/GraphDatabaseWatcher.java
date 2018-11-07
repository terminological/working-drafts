package uk.co.terminological.literaturereview;

import java.util.List;

import uk.co.terminological.pipestream.Event;
import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.Metadata;

public class GraphDatabaseWatcher<Y> extends EventGenerator.Default<Y> {

	public GraphDatabaseWatcher(Metadata metadata) {
		super(metadata);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Event<Y>> generate() {
		// TODO Auto-generated method stub
		return null;
	}

}
