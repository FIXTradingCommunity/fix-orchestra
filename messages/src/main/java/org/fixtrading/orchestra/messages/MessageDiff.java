package org.fixtrading.orchestra.messages;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageDiff {

	public class DefaultListener implements MessageDiffListener {

		private final PrintStream out;
		
		public DefaultListener(PrintStream out) {
			this.out = out;
		}

		@Override
		public void accept(Source source, String description) {
			char sourceIndicator;
			switch (source) {
			case FIRST_SOURCE:
				sourceIndicator = '<';
				break;
			case SECOND_SOURCE:
				sourceIndicator = '>';
				break;
			default:
				sourceIndicator = '=';

			}
			out.format("%c %s\n", sourceIndicator, description);
		}

	}

	public static void main(String[] args) {
		if (args.length < 2) {
			usage();
		} else {
			try {
				MessageDiff tool = new MessageDiff();
				tool.init();
				FileInputStream inputStream1 = new FileInputStream(args[0]);
				FileInputStream inputStream2 = new FileInputStream(args[1]);
				tool.diff(inputStream1, inputStream2);
			} catch(Exception e) {
				
			}
		}
	}
	/**
	 * Prints application usage
	 */
	public static void usage() {
		System.out.println("Usage: MessageDiffTool <ontology-file1> <ontology-file2>");
	}
	private MessageDiffListener listener = new DefaultListener(System.out);
	private Model model1;

	private Model model2;

	private final MessageOntologyManager ontologyManager = new MessageOntologyManager();

	public void diff(InputStream inputStream1, InputStream inputStream2) throws Exception {
		model1 = ontologyManager.loadModel(inputStream1);
		model2 = ontologyManager.loadModel(inputStream2);
		
		Set<MessageEntity> messageSet1 = ontologyManager.getMessages(model1);
		Set<MessageEntity> messageSet2 = ontologyManager.getMessages(model2);
		
		Set<MessageEntity> intersection = messageSet1.stream().filter(m-> messageSet2.contains(m)).collect(Collectors.toSet());
		Set<MessageEntity> in1only = messageSet1.stream().filter(m -> !messageSet2.contains(m)).collect(Collectors.toSet());
		Set<MessageEntity> in2only = messageSet2.stream().filter(m -> !messageSet1.contains(m)).collect(Collectors.toSet());
		
		in1only.forEach(m -> listener.accept(MessageDiffListener.Source.FIRST_SOURCE, m.getName()));
		in2only.forEach(m -> listener.accept(MessageDiffListener.Source.SECOND_SOURCE, m.getName()));
		intersection.forEach(m -> listener.accept(MessageDiffListener.Source.EQUAL, m.getName()));
	}

	/**
	 * Initializes resources
	 * 
	 * @throws Exception
	 *             if resources cannot be initialized
	 */
	public void init() throws Exception {
		ontologyManager.init();
	}

	public void setListener(MessageDiffListener listener) {
		this.listener = listener;
	}
}
