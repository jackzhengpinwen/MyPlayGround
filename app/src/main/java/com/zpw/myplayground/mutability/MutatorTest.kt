package com.zpw.myplayground.mutability

fun testMutator() {
    val nation = Nation("china", "86")
    val address = Address(nation, "shenzhen", "nanshan")
    val person = Person("zpw", address)

    val person11 = person.copy(
        name = "zpy"
    )

    val person12 = person.mutate(Person::name) {
        "zpy"
    }

    val person21 = person.copy(
        address = person.address.copy(
            street = "quangang"
        )
    )

    val person22 = person.mutate(Person::address, Address::street) {
        "quangang"
    }

    val person31 = person.copy(
        address = person.address.copy(
            nation = person.address.nation.copy(
                code = "886"
            )
        )
    )

    val person32 = person.mutate(Person::address, Address::nation, Nation::code) {
        "886"
    }
}

data class Person(
    val name: String,
    val address: Address
)

data class Address(
    val nation: Nation,
    val state: String,
    val street: String
)

data class Nation(
    val name: String,
    val code: String
)