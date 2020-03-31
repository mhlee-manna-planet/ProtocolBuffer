package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.example.tutorial.PersonProto.AddressBook;
import com.example.tutorial.PersonProto.AddressBook.Builder;
import com.example.tutorial.PersonProto.Person;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProtoOrBuilder;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class MainTest {
	static final String AddressBookCSharpFilePath = "../ProtoFiles/AddressBook.csharp.data";
	static final String AddressBookJavaFilePath = "../ProtoFiles/AddressBook.java.data";
	static final String AddressBookProtoFilePath = "../ProtoFiles/AddressBook.proto";
	static final String AddressBookDescriptorFilePath = "../ProtoFiles/AddressBook.pb";
	static final String PersonDescriptorFilePath = "../ProtoFiles/Person.desc";

	public static void main(String[] args) {
		createAddressBook();
		loadAddressBook(AddressBookJavaFilePath);
		loadAddressBook(AddressBookCSharpFilePath);
		fieldAccessTest();
		dynamicMessageTest();
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
	
	/**
	 * ���ϰ� �ʵ� ��ȸ�� �� �ִ� ����� �ִ��� ����
	 */
	private static void fieldAccessTest() {
		//
		// ������ Ÿ�ӿ� ��ü�� ���� ������ �˰� ���� ��
		//
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
		System.out.println();

		//
		// Person Descriptor�� ���Ͽ� ����
		//
		try (FileOutputStream os = new FileOutputStream(PersonDescriptorFilePath)) {
			desc.toProto().writeTo(os);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//
		// Person Descriptor ���Ͽ��� �ε�
		//
		try (FileInputStream is = new FileInputStream(PersonDescriptorFilePath)) {
			DescriptorProto dp = DescriptorProto.parseFrom(is);
			for (var field : dp.getFieldList()) {
				System.out.format("Field, Name: %s, Number: %d\n", field.getName(), field.getNumber());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	
	/**
	 * ��Ÿ�ӿ� �������ݹ��� ��ũ���� ����� �޽��� �����ϱ�
	 */
	private static void dynamicMessageTest() {
		System.out.println("========== dynamicMessageTest ==========");
		
		//
		// �������ݹ��� ������ ����ִ� .pb ���� �ε�
		//
		try (FileInputStream is = new FileInputStream(AddressBookDescriptorFilePath)) {
			FileDescriptorSet fds = FileDescriptorSet.parseFrom(is);
			List<FileDescriptorProto> fdps = fds.getFileList();
			for (var fdp : fdps) {
				//
				// FileDescriptor ȹ��
				//
				FileDescriptor fd = FileDescriptor.buildFrom(fdp, new FileDescriptor[0]);
				
				//
				// FileDescriptor�κ��� ���� �޽��� Ÿ�� ȹ�� (AddressBook, Person ���)
				//
				Descriptor personDesc = fd.findMessageTypeByName("Person");
				Descriptor addressBookDesc = fd.findMessageTypeByName("AddressBook");
				if (personDesc == null || addressBookDesc == null) {
					System.out.println("Can't find message type.");
					continue;
				}
				
				//
				// ���̳��� �޽��� ���·� ����
				//
				DynamicMessage.Builder personBuilder = DynamicMessage.newBuilder(personDesc);
				personBuilder.setField(personDesc.getFields().get(0), "ȫ�浿");
				personBuilder.setField(personDesc.getFields().get(1), 18);
				personBuilder.setField(personDesc.getFields().get(2), "gdhong@example.com");
				byte[] personData = personBuilder.build().toByteArray();
				
				//
				// ����Ʈ �迭�� �Ľ��ؼ� ���̳��� �޽��� ����
				//
				DynamicMessage person = DynamicMessage.parseFrom(personDesc, personData);
				for (var elem : person.getAllFields().entrySet()) {
					System.out.format("DynamicMessage, Type:%s, FieldName:%s, FieldValue:%s\n", personDesc.getName(), elem.getKey().getName(), elem.getValue().toString());
				}
				
				//
				// AddressBook�� ���̳��� �޽��� ���·� ���� (�ݺ� �ʵ� �� ����)
				//
				DynamicMessage.Builder addressBookBuilder = DynamicMessage.newBuilder(addressBookDesc);
				FieldDescriptor field = addressBookDesc.findFieldByName("people");
				{
					DynamicMessage.Builder john = addressBookBuilder.newBuilderForField(field);
					john.setField(personDesc.getFields().get(0), "John");
					john.setField(personDesc.getFields().get(1), 40);
					john.setField(personDesc.getFields().get(2), "john@example.com");
					addressBookBuilder.addRepeatedField(field, john.build());
					
					DynamicMessage.Builder jane = addressBookBuilder.newBuilderForField(field);
					jane.setField(personDesc.getFields().get(0), "Jane");
					jane.setField(personDesc.getFields().get(1), 33);
					jane.setField(personDesc.getFields().get(3), 150_000);
					addressBookBuilder.addRepeatedField(field, jane.build());
				}
				byte[] addressBookData = addressBookBuilder.build().toByteArray();
				
				//
				// ����Ʈ �迭�� �Ľ��ؼ� ���̳��� �޽��� ���� (�Ϲ����� Ÿ���� AddressBook)
				//
				DynamicMessage addressBook = DynamicMessage.parseFrom(addressBookDesc, addressBookData);
				for (var elem : addressBook.getAllFields().entrySet()) {
					System.out.format("DynamicMessage, Type:%s, FieldName:%s, FieldValue:%s\n", addressBookDesc.getName(), elem.getKey().getName(), elem.getValue().toString());
				}
				
				System.out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DescriptorValidationException e) {
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
	 * �ʵ� ����Ʈ���� �ʵ� ã��
	 */
	private static FieldDescriptorProto findFieldByName(List<FieldDescriptorProto> fields, String fieldName) {
		for (var field : fields) {
			if (field.getName().compareTo(fieldName) == 0) {
				return field;
			}
		}
		return null;
	}
}
