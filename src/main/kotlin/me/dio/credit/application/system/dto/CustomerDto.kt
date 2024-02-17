package me.dio.credit.application.system.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import org.hibernate.validator.constraints.br.CPF
import java.math.BigDecimal

data class CustomerDto(
    @field:NotEmpty(message = "First Name cannot be empty") val firstName: String,
    @field:NotEmpty(message = "Last Name cannot be empty") val lastName: String,
    @field:NotEmpty(message = "CPF cannot be empty")
    @field:CPF(message = "Error,please insert a valid CPF") val cpf: String,
    @field:NotNull(message = "Income cannot be empty") val income: BigDecimal,
    @field:Email(message = "Error,insert a valid Email")
    @field:NotEmpty(message = "Email cannot be empty") val email: String,
    @field:NotEmpty(message = "Password Name cannot be empty") val password: String,
    @field:NotEmpty(message = "Zip Code Name cannot be empty") val zipCode: String,
    @field:NotEmpty(message = "Street cannot be empty") val street: String
) {

    fun toEntity(): Customer = Customer(
        firstName = this.firstName,
        lastName = this.lastName,
        cpf = this.cpf,
        income = this.income,
        email = this.email,
        password = this.password,
        address = Address(
            zipCode = this.zipCode,
            street = this.street
        )

    )

}
