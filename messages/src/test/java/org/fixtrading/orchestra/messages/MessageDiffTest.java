package org.fixtrading.orchestra.messages;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

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
		manager.createMessage(model, 1, "MessageOne", "A");
		manager.createMessage(model, 2, "MessageTwo", "B");
		manager.createMessage(model, 3, "MessageThree", "C");
		manager.createMessage(model, 4, "MessageFour", "D");
		manager.storeModel(model, new FileOutputStream(filename1));
	}

	private static void createModel2() throws Exception {
		MessageOntologyManager manager = new MessageOntologyManager();
		manager.init();
		Model model = manager.createNewModel("mod2", new URI("http://test2"));
		manager.createMessage(model, 1, "MessageOne", "A");
		manager.createMessage(model, 3, "MessageThree", "C");
		manager.createMessage(model, 4, "MessageFour", "D");
		manager.createMessage(model, 5, "MessageFive", "E");
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
