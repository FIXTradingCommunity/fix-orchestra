package org.fixtrading.orchestra.messages;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.fixtrading.orchestra.messages.MessageOntologyManager.CodeObject;
import org.fixtrading.orchestra.messages.MessageOntologyManager.CodeSetObject;
import org.fixtrading.orchestra.messages.MessageOntologyManager.DataTypeObject;
import org.fixtrading.orchestra.messages.MessageOntologyManager.FieldObject;

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

	/**
	 * Compares two message ontologies. By default, report is sent to console.
	 * 
	 * @param args
	 *            file names of two ontologies to compare
	 */
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
			} catch (Exception e) {

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

		Set<MessageEntity> intersection = messageSet1.stream().filter(m -> messageSet2.contains(m))
				.collect(Collectors.toSet());
		Set<MessageEntity> in1only = messageSet1.stream().filter(m -> !messageSet2.contains(m))
				.collect(Collectors.toSet());
		Set<MessageEntity> in2only = messageSet2.stream().filter(m -> !messageSet1.contains(m))
				.collect(Collectors.toSet());

		in1only.forEach(m -> listener.accept(MessageDiffListener.Source.FIRST_SOURCE, m.getName()));
		in2only.forEach(m -> listener.accept(MessageDiffListener.Source.SECOND_SOURCE, m.getName()));

		intersection.forEach(m -> diff(m, ontologyManager.getMessage(model2, m.getName())));
	}

	public void diff(FieldObject field1, FieldObject field2) {
		DataTypeObject type1 = field1.getDataType();
		DataTypeObject type2 = field2.getDataType();
		if (!type1.equals(type2)) {
			listener.accept(MessageDiffListener.Source.FIRST_SOURCE,
					String.format("Datatype of %s=%s", field1.getName(), type1.getName()));
			listener.accept(MessageDiffListener.Source.SECOND_SOURCE,
					String.format("Datatype of %s=%s", field2.getName(), type2.getName()));
		} else if (type1 instanceof CodeSetObject) {
			CodeSetObject cs1 = (CodeSetObject) type1;
			CodeSetObject cs2 = (CodeSetObject) type2;
			
			Set<CodeObject> codes1 = ontologyManager.getCodes(model1, cs1.getName());
			Set<CodeObject> codes2 = ontologyManager.getCodes(model2, cs2.getName());
			
			Set<CodeObject> CodeIn1only = codes1.stream().filter(m -> !codes2.contains(m)).collect(Collectors.toSet());
			Set<CodeObject> CodeIn2only = codes2.stream().filter(m -> !codes1.contains(m)).collect(Collectors.toSet());

			CodeIn1only.forEach(c -> listener.accept(MessageDiffListener.Source.FIRST_SOURCE,
					String.format("Code %s/%s [%s]", field1.getName(), c.getName(), c.getValue())));
			CodeIn2only.forEach(c -> listener.accept(MessageDiffListener.Source.SECOND_SOURCE,
					String.format("Code %s/%s [%s]", field1.getName(), c.getName(), c.getValue())));


		}
	}

	public void diff(MessageEntity msg1, MessageEntity msg2) {
		Set<FieldObject> req1 = new HashSet<>();
		ontologyManager.getRequiredFields(msg1, req1);
		Set<FieldObject> req2 = new HashSet<>();
		ontologyManager.getRequiredFields(msg2, req2);

		Set<FieldObject> reqIntersection = req1.stream().filter(m -> req2.contains(m)).collect(Collectors.toSet());
		Set<FieldObject> ReqIn1only = req1.stream().filter(m -> !req2.contains(m)).collect(Collectors.toSet());
		Set<FieldObject> ReqIn2only = req2.stream().filter(m -> !req1.contains(m)).collect(Collectors.toSet());

		ReqIn1only.forEach(m -> listener.accept(MessageDiffListener.Source.FIRST_SOURCE,
				String.format("Required %s/%s", msg1.getName(), m.getName())));
		ReqIn2only.forEach(m -> listener.accept(MessageDiffListener.Source.SECOND_SOURCE,
				String.format("Required %s/%s", msg1.getName(), m.getName())));

		for (FieldObject field : reqIntersection) {
			diff(field, ontologyManager.getField(model2, field.getName()));
		}

		Set<FieldObject> opt1 = new HashSet<>();
		ontologyManager.getOptionalFields(msg1, opt1);
		Set<FieldObject> opt2 = new HashSet<>();
		ontologyManager.getOptionalFields(msg2, opt2);

		Set<FieldObject> optIntersection = opt1.stream().filter(m -> opt2.contains(m)).collect(Collectors.toSet());
		Set<FieldObject> OptIn1only = opt1.stream().filter(m -> !opt2.contains(m)).collect(Collectors.toSet());
		Set<FieldObject> OptIn2only = opt2.stream().filter(m -> !opt1.contains(m)).collect(Collectors.toSet());

		OptIn1only.forEach(m -> listener.accept(MessageDiffListener.Source.FIRST_SOURCE,
				String.format("Optional %s/%s", msg1.getName(), m.getName())));
		OptIn2only.forEach(m -> listener.accept(MessageDiffListener.Source.SECOND_SOURCE,
				String.format("Optional %s/%s", msg1.getName(), m.getName())));

		for (FieldObject field : optIntersection) {
			diff(field, ontologyManager.getField(model2, field.getName()));
		}
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

	/**
	 * Registers a listener for ontology differences. If one is not registered,
	 * a default listener sends reports to the console.
	 * 
	 * @param listener
	 *            a listener
	 */
	public void setListener(MessageDiffListener listener) {
		this.listener = listener;
	}
}
