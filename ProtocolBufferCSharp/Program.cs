using ProtoBuf;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Tutorial;

namespace ProtocolBufferCSharp
{
	class Program
	{
		static readonly string AddressBookCSharpFilePath = @"..\..\..\ProtoFiles\AddressBook.csharp.data";
		static readonly string AddressBookJavaFilePath = @"..\..\..\ProtoFiles\AddressBook.java.data";

		static void Main(string[] args)
		{
			CreateAddressBook();
			LoadAddressBook(AddressBookCSharpFilePath);
			LoadAddressBook(AddressBookJavaFilePath);
		}

		private static void CreateAddressBook()
		{
			Console.WriteLine("========== Create Address Book ==========");

			Person john = new Person
			{
				Name = "John Doe",
				Id = 1,
				Email = "john@example.com"
			};
			PrintPerson(john);

			Person jane = new Person
			{
				Name = "Jane Fonda",
				Id = 2,
				Money = 100_000
			};
			PrintPerson(jane);

			AddressBook ab = new AddressBook();
			{
				ab.Peoples.Add(john);
				ab.Peoples.Add(jane);
			}

			using (var file = File.Create(AddressBookCSharpFilePath))
			{
				Serializer.Serialize(file, ab);
			}

			Console.WriteLine();
		}

		private static void LoadAddressBook(string path)
		{
			Console.WriteLine($"========== Load Address Book : {Path.GetFileName(path)} ==========");

			if (!File.Exists(path))
			{
				return;
			}

			using (var file = File.OpenRead(path))
			{
				AddressBook addressBook = Serializer.Deserialize<AddressBook>(file);
				addressBook.Peoples.ForEach(p => PrintPerson(p));
			}

			Console.WriteLine();
		}

		private static void PrintPerson(Person person)
		{
			Console.WriteLine($"Person, Name:{person.Name}, Id:{person.Id}, Email:{person.Email}, EmailSerialized:{person.ShouldSerializeEmail()}, Money:{person.Money}, MoneySerialized:{person.ShouldSerializeMoney()}");
		}
	}
}
