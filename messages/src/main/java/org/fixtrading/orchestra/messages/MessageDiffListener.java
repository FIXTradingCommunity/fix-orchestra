package org.fixtrading.orchestra.messages;

import java.util.function.BiConsumer;

public interface MessageDiffListener extends BiConsumer<MessageDiffListener.Source, String>{

	enum Source { FIRST_SOURCE, SECOND_SOURCE, EQUAL };
	
}
