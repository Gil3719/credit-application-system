package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.dto.CreditViewList
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Collectors


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired private lateinit var creditRepository: CreditRepository
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var customerRepository: CustomerRepository

    companion object {
        const val URl: String = "/api/credits"
    }

    @BeforeEach fun setup() = creditRepository.deleteAll()

    @AfterEach fun tearDown() = creditRepository.deleteAll()

    @Test
    fun `should create a credit and return 201 status`(){
        //given
        val fakeCustomer: Customer = buildCustomer()
        customerRepository.save(fakeCustomer)
        val fakeCreditDto: CreditDto = buildCreditDto()
        val valueAssString: String = objectMapper.writeValueAsString(fakeCreditDto)
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post(URl).contentType(MediaType.APPLICATION_JSON)
            .content(valueAssString))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value("5000.0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.dayFirstOfInstallment")
                .value("2024-04-16"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallments").value("15"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.customerId").value("1"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create a credit with day of first installment later then 3 months and return 400 status`(){
        //given
        val fakeCustomer: Customer = buildCustomer()
        customerRepository.save(fakeCustomer)
        val fakeCreditDto: CreditDto = buildCreditDto(dayFirstOfInstallment = LocalDate.now().plusMonths(5))
        val valueAssString: String = objectMapper.writeValueAsString(fakeCreditDto)
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post(URl).contentType(MediaType.APPLICATION_JSON)
            .content(valueAssString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the Documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value("class me.dio.credit.application.system.exception.BusinessException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save credit if number of installments is bigger then 48 and return 400 status`(){
        //given
        val fakeCustomer: Customer = buildCustomer()
        customerRepository.save(fakeCustomer)
        val fakeCreditDto: CreditDto = buildCreditDto(numberOfInstallments = 49)
        val valueAssString: String = objectMapper.writeValueAsString(fakeCreditDto)
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.post(URl).contentType(MediaType.APPLICATION_JSON)
            .content(valueAssString))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the Documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception")
                .value("class org.springframework.web.bind.MethodArgumentNotValidException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find all credits by customer id and return 200 status`(){
        //given
        val fakeCustomer: Customer = buildCustomer()
        customerRepository.save(fakeCustomer)
        val fakeCreditDto: CreditDto = buildCreditDto()
        val fake2CreditDto: CreditDto = buildCreditDto()
        val valueAssString: String = objectMapper.writeValueAsString(fakeCreditDto)
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("$URl?customerId=${fakeCustomer.id}")
            .accept(MediaType.APPLICATION_JSON).content(valueAssString))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }


    private fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(5000.0),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(2),
        numberOfInstallments: Int = 15,
        customer: Customer = buildCustomer(),
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customer.id!!
    )

    private fun buildCustomer(
        firstName: String = "Gil",
        lastName: String = "teste",
        cpf: String = "28475934625",
        email: String = "email@teste.com",
        password: String = "12345",
        zipCode: String = "cep",
        street: String = "Rua",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        id: Long = 1
    ):Customer = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street,
        ),
        income = income,
        id = id

    )
}