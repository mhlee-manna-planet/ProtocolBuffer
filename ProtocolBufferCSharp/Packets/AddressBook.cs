// This file was generated by a tool; you should avoid making direct changes.
// Consider using 'partial classes' to extend these types
// Input: AddressBook.proto

#pragma warning disable CS1591, CS0612, CS3021, IDE1006
namespace Tutorial
{

    [global::ProtoBuf.ProtoContract()]
    public partial class Person : global::ProtoBuf.IExtensible
    {
        private global::ProtoBuf.IExtension __pbn__extensionData;
        global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
            => global::ProtoBuf.Extensible.GetExtensionObject(ref __pbn__extensionData, createIfMissing);

        [global::ProtoBuf.ProtoMember(1, Name = @"name", IsRequired = true)]
        public string Name { get; set; }

        [global::ProtoBuf.ProtoMember(2, Name = @"id", IsRequired = true)]
        public int Id { get; set; }

        [global::ProtoBuf.ProtoMember(3, Name = @"email")]
        [global::System.ComponentModel.DefaultValue("")]
        public string Email
        {
            get { return __pbn__Email ?? ""; }
            set { __pbn__Email = value; }
        }
        public bool ShouldSerializeEmail() => __pbn__Email != null;
        public void ResetEmail() => __pbn__Email = null;
        private string __pbn__Email;

        [global::ProtoBuf.ProtoMember(4, Name = @"money")]
        public int Money
        {
            get { return __pbn__Money.GetValueOrDefault(); }
            set { __pbn__Money = value; }
        }
        public bool ShouldSerializeMoney() => __pbn__Money != null;
        public void ResetMoney() => __pbn__Money = null;
        private int? __pbn__Money;

    }

    [global::ProtoBuf.ProtoContract()]
    public partial class AddressBook : global::ProtoBuf.IExtensible
    {
        private global::ProtoBuf.IExtension __pbn__extensionData;
        global::ProtoBuf.IExtension global::ProtoBuf.IExtensible.GetExtensionObject(bool createIfMissing)
            => global::ProtoBuf.Extensible.GetExtensionObject(ref __pbn__extensionData, createIfMissing);

        [global::ProtoBuf.ProtoMember(1, Name = @"people")]
        public global::System.Collections.Generic.List<Person> Peoples { get; } = new global::System.Collections.Generic.List<Person>();

    }

}

#pragma warning restore CS1591, CS0612, CS3021, IDE1006
