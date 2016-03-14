package org.fixtrading.orchestra.messages;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

import org.fixtrading.orchestra.messages.MessageOntologyManager.MessageObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageDiffTest {

	private static final String filename1 = "Model1.rdf";
	private static final String filename2 = "Model2.rdf";
	private MessageDiff tool;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		createModel1();
		createModel2();
	}

	private static void createModel1() throws Exception {
		MessageOntologyManager manager = new MessageOntologyManager();
		manager.init();
		Model model = manager.createNewModel("mod1", new URI("http://test1"));
		MessageObject msg1 = manager.createMessage(model, 1, "MessageOne", "A");
		MessageObject msg2 = manager.createMessage(model, 2, "MessageTwo", "B");
		MessageObject msg3 = manager.createMessage(model, 3, "MessageThree", "C");
		MessageObject msg4 = manager.createMessage(model, 4, "MessageFour", "D");
		
		MessageEntity fld1 = manager.createField(model, 101, "FieldOne", "int");
		MessageEntity fld2 = manager.createField(model, 102, "FieldTwo", "int");
		MessageEntity fld3 = manager.createField(model, 103, "FieldThree", "int");
		MessageEntity fld4 = manager.createField(model, 104, "FieldFour", "int");
		
		manager.addField(msg1, 101, "FieldOne", true);
		manager.addField(msg1, 102, "FieldTwo", false);
		manager.addField(msg1, 103, "FieldThree", true);
		manager.addField(msg1, 104, "FieldFour", false);

		manager.storeModel(model, new FileOutputStream(filename1));
	}

	private static void createModel2() throws Exception {
		MessageOntologyManager manager = new MessageOntologyManager();
		manager.init();
		Model model = manager.createNewModel("mod2", new URI("http://test2"));
		MessageObject msg1 = manager.createMessage(model, 1, "MessageOne", "A");
		manager.createMessage(model, 3, "MessageThree", "C");
		manager.createMessage(model, 4, "MessageFour", "D");
		manager.createMessage(model, 5, "MessageFive", "E");
		
		MessageEntity fld1 = manager.createField(model, 101, "FieldOne", "int");
		MessageEntity fld2 = manager.createField(model, 102, "FieldTwo", "int");
		MessageEntity fld3 = manager.createField(model, 103, "FieldThree", "int");
		MessageEntity fld4 = manager.createField(model, 104, "FieldFour", "int");
		MessageEntity fld5 = manager.createField(model, 105, "FieldFive", "int");
		
		manager.addField(msg1, 101, "FieldOne", true);
		manager.addField(msg1, 102, "FieldTwo", false);
		manager.addField(msg1, 103, "FieldThree", false);
		manager.addField(msg1, 104, "FieldFour", false);
		manager.addField(msg1, 105, "FieldFive", false);
		
		manager.storeModel(model, new FileOutputStream(filename2));

	
		
	}

	@Before
	public void setUp() throws Exception {
		tool = new MessageDiff();
		tool.init();
	}

	@Test
	public void testDiff() throws Exception {
		tool.diff(new FileInputStream(filename1), new FileInputStream(filename2));
	}

}
