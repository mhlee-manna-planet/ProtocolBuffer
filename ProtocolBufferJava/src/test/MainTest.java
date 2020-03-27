package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.example.tutorial.PersonProto.AddressBook;
import com.example.tutorial.PersonProto.AddressBook.Builder;
import com.example.tutorial.PersonProto.Person;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;

public class MainTest {
	static final String AddressBookCSharpFilePath = "../ProtoFiles/AddressBook.csharp.data";
	static final String AddressBookJavaFilePath = "../ProtoFiles/AddressBook.java.data";

	public static void main(String[] args) {
		createAddressBook();
		loadAddressBook(AddressBookJavaFilePath);
		loadAddressBook(AddressBookCSharpFilePath);
		fieldAccessTest();
	}

	private static void createAddressBook() {
		System.out.println("========== Create Address Book ==========");
		
		Person johnSmith = 
			Person.newBuilder()
				.setName("John Smith")
				.setId(5)
				.setMoney(70000)
				.build();
		printPerson(johnSmith);
		
		Person janeSmith = 
				Person.newBuilder()
					.setName("Jane Smith")
					.setId(6)
					.setEmail("jane.smith@sample.com")
					.build();
		printPerson(janeSmith);
		
		AddressBook ab =
			AddressBook.newBuilder()
				.addPeople(johnSmith)
				.addPeople(janeSmith)
				.build();
		
		try (FileOutputStream os = new FileOutputStream(AddressBookJavaFilePath)) {
			ab.writeTo(os);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println();
	}
	
	private static void loadAddressBook(String path) {
		File file = new File(path);

		System.out.println(
			String.format("========== Load Address Book : %s ==========",
				file.getName()));
		
		if (!file.exists()) {
			return;
		}
		
		try (FileInputStream is = new FileInputStream(file)) {
			AddressBook ab = AddressBook.parseFrom(is);
			for (int i = 0; i < ab.getPeopleCount(); i++) {
				printPerson(ab.getPeople(i));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println();
	}
	
	private static void printPerson(Person person) {
		System.out.println(
			String.format("Person, Name:%s, Id:%d, Email:%s, EmailSerialized:%s, Money:%d, MoneySerialized:%s",
					person.getName(), person.getId(), person.getEmail(), person.hasEmail(), person.getMoney(), person.hasMoney()));
	}

	/**
	 * ���ϰ� �ʵ� ��ȸ�� �� �ִ� ����� �ִ��� ����
	 */
	private static void fieldAccessTest() {
		// Person ��ü�� ���� Descriptor ȹ��
		Descriptor desc = Person.getDescriptor();
		// Person ��ü ���� ����
		com.example.tutorial.PersonProto.Person.Builder personBuilder = Person.newBuilder();

		//
		// �ʵ� �̸����� ����
		//
		// "name" �ʵ忡 ���� Descriptor ȹ��
		FieldDescriptor nameField = desc.findFieldByName("name");
		// "name" �ʵ忡 �� ���� (�տ��� ȹ���� FieldDescriptor �̿�)
		personBuilder.setField(nameField, "JJ");
		
		//
		// �ʵ� ��ȣ�� ����
		//
		// "id" �ʵ� ��ȣ�� Descriptor ȹ��
		FieldDescriptor idField = desc.findFieldByNumber(2);
		// "id" �ʵ忡 �� ����
		personBuilder.setField(idField, 43);
		
		// Person ��ü ����
		Person person = personBuilder.buildPartial();
		
		printPerson(person);
	}

}
