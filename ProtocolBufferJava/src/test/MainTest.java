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
	 * 편리하게 필드 조회할 수 있는 방법이 있는지 조사
	 */
	private static void fieldAccessTest() {
		//
		// 컴파일 타임에 객체에 대한 정보를 알고 있을 때
		//
		// Person 객체에 대한 Descriptor 획득
		Descriptor desc = Person.getDescriptor();
		// Person 객체 빌더 생성
		com.example.tutorial.PersonProto.Person.Builder personBuilder = Person.newBuilder();

		//
		// 필드 이름으로 설정
		//
		// "name" 필드에 대한 Descriptor 획득
		FieldDescriptor nameField = desc.findFieldByName("name");
		// "name" 필드에 값 설정 (앞에서 획득한 FieldDescriptor 이용)
		personBuilder.setField(nameField, "JJ");
		
		//
		// 필드 번호로 설정
		//
		// "id" 필드 번호로 Descriptor 획득
		FieldDescriptor idField = desc.findFieldByNumber(2);
		// "id" 필드에 값 설정
		personBuilder.setField(idField, 43);
		
		// Person 객체 빌드
		Person person = personBuilder.buildPartial();
		
		printPerson(person);
		System.out.println();

		//
		// Person Descriptor를 파일에 저장
		//
		try (FileOutputStream os = new FileOutputStream(PersonDescriptorFilePath)) {
			desc.toProto().writeTo(os);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//
		// Person Descriptor 파일에서 로드
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
	 * 런타임에 프로토콜버퍼 디스크립터 기반의 메시지 생성하기
	 */
	private static void dynamicMessageTest() {
		System.out.println("========== dynamicMessageTest ==========");
		
		//
		// 프로토콜버퍼 정보가 들어있는 .pb 파일 로드
		//
		try (FileInputStream is = new FileInputStream(AddressBookDescriptorFilePath)) {
			FileDescriptorSet fds = FileDescriptorSet.parseFrom(is);
			List<FileDescriptorProto> fdps = fds.getFileList();
			for (var fdp : fdps) {
				//
				// FileDescriptor 획득
				//
				FileDescriptor fd = FileDescriptor.buildFrom(fdp, new FileDescriptor[0]);
				
				//
				// FileDescriptor로부터 실제 메시지 타입 획득 (AddressBook, Person 등등)
				//
				Descriptor personDesc = fd.findMessageTypeByName("Person");
				Descriptor addressBookDesc = fd.findMessageTypeByName("AddressBook");
				if (personDesc == null || addressBookDesc == null) {
					System.out.println("Can't find message type.");
					continue;
				}
				
				//
				// 다이내믹 메시지 형태로 빌드
				//
				DynamicMessage.Builder personBuilder = DynamicMessage.newBuilder(personDesc);
				personBuilder.setField(personDesc.getFields().get(0), "홍길동");
				personBuilder.setField(personDesc.getFields().get(1), 18);
				personBuilder.setField(personDesc.getFields().get(2), "gdhong@example.com");
				byte[] personData = personBuilder.build().toByteArray();
				
				//
				// 바이트 배열을 파싱해서 다이내믹 메시지 생성
				//
				DynamicMessage person = DynamicMessage.parseFrom(personDesc, personData);
				for (var elem : person.getAllFields().entrySet()) {
					System.out.format("DynamicMessage, Type:%s, FieldName:%s, FieldValue:%s\n", personDesc.getName(), elem.getKey().getName(), elem.getValue().toString());
				}
				
				//
				// AddressBook을 다이내믹 메시지 형태로 빌드 (반복 필드 값 설정)
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
				// 바이트 배열을 파싱해서 다이내믹 메시지 생성 (암묵적인 타겟은 AddressBook)
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
	 * 필드 리스트에서 필드 찾기
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
