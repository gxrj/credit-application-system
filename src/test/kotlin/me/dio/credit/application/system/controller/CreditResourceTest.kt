package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `should save a credit request then return a message and 201 status`() {
        //input
        val fakeCredit = mockCredit( BigDecimal.valueOf( 500000 ) , 25 )

        //test
        val performedResult = mockMvc.perform(
                    MockMvcRequestBuilders.post( URL )
                        .contentType( "application/json" )
                        .content( fakeCredit )
                )
                .andExpect( MockMvcResultMatchers.status().isCreated )

        val mockedCreditCode = creditRepository.findAllByCustomerId( 1L )[0].creditCode
        val expectedMessage = "Credit $mockedCreditCode - Customer camila@email.com saved!"
        val actualMessage = performedResult.andReturn().response.contentAsString

        Assertions.assertEquals( expectedMessage, actualMessage, "Content conflict!" )
        //output
    }

    @Test
    fun `should not save a credit with dayFirstInstallment longer than 3 months then return 400 status`() {
        //input
        val fakeCredit = mockCredit(
                            numberOfInstallments = 25,
                            creditValue =  BigDecimal.valueOf( 500000 ),
                            dayFirstOfInstallment = LocalDate.now().plusDays( 91 ) )

        //test
        mockMvc.perform(
            MockMvcRequestBuilders.post( URL )
                .contentType( "application/json" )
                .content( fakeCredit )
        )
        .andExpect( MockMvcResultMatchers.status().isBadRequest )
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the documentation")
        )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.timestamp" ).exists() )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.status" ).value( 400 ) )
        .andExpect(
            MockMvcResultMatchers.jsonPath( "$.exception" )
                .value( "class me.dio.credit.application.system.exception.BusinessException" )
        )
        .andExpect(
            MockMvcResultMatchers
                .jsonPath( "$.details[*]" )
                .value( "Invalid Date" )
        )

        //output
        .andDo( MockMvcResultHandlers.print() )
    }

    @Test
    fun `should not save a credit with a invalid customerId and return 400 status`() {
        //input
        val invalidCustomerId = 330L

        val fakeCredit = mockCredit(
            customerId = invalidCustomerId,
            numberOfInstallments = 25,
            creditValue =  BigDecimal.valueOf( 500000 ) )

        //test
        mockMvc.perform(
            MockMvcRequestBuilders.post( URL )
                .contentType( "application/json" )
                .content( fakeCredit )
        )
        .andExpect( MockMvcResultMatchers.status().isBadRequest )
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the documentation")
        )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.timestamp" ).exists() )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.status" ).value( 400 ) )
        .andExpect(
            MockMvcResultMatchers.jsonPath( "$.exception" )
                .value( "class me.dio.credit.application.system.exception.BusinessException" )
        )
        .andExpect(
            MockMvcResultMatchers
                .jsonPath( "$.details[*]" )
                .value( "Id $invalidCustomerId not found" )
        )

        //output
        .andDo( MockMvcResultHandlers.print() )
    }

    @Test
    fun `should not save a credit with invalid numberOfInstallments then return 400 status`() {
        //input

        val invalidNumberOfInstalments = 49

        val fakeCredit = mockCredit(
            creditValue =  BigDecimal.valueOf( 500000 ),
            numberOfInstallments = invalidNumberOfInstalments )

        val expectedErrorMsg = if( invalidNumberOfInstalments > 48 ) "must be less than or equal to 48"
                               else "must be greater than or equal to 1"
        //test
        mockMvc.perform(
            MockMvcRequestBuilders.post( URL )
                .contentType( "application/json" )
                .content( fakeCredit )
        )
        .andExpect( MockMvcResultMatchers.status().isBadRequest )
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the documentation")
        )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.timestamp" ).exists() )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.status" ).value( 400 ) )
        .andExpect(
            MockMvcResultMatchers.jsonPath( "$.exception" )
                .value( "class org.springframework.web.bind.MethodArgumentNotValidException" )
        )
        .andExpect(
            MockMvcResultMatchers
                .jsonPath( "$.details[*]" )
                .value( expectedErrorMsg )
        )

        //output
        .andDo( MockMvcResultHandlers.print() )
    }

    @Test
    fun `should find a credit request by a credit code then return 200 status`() {
        //input
        val fakeCustomer = buildFakeCustomer()

        val fakeCredit = Credit(
            customer = fakeCustomer,
            creditValue = BigDecimal.valueOf( 50000.0 ),
            dayFirstInstallment = LocalDate.now().plusDays( 45 ) )

        val creditCode = creditRepository.save( fakeCredit ).creditCode

        //test
        mockMvc.perform(
            MockMvcRequestBuilders
                .get( "$URL/$creditCode?customerId=${ fakeCustomer.id }" )
                .accept( "application/json" )
        )
        .andExpect( MockMvcResultMatchers.status().isOk )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.creditCode" ).value( creditCode.toString() ) )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.creditValue" ).value( fakeCredit.creditValue ) )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.emailCustomer" ).value( fakeCustomer.email ) )

        //output
        .andDo( MockMvcResultHandlers.print() )
    }

    @Test
    fun `should not find credit with a invalid creditCode then return 400 status`() {
        //input
        val fakeCustomer = buildFakeCustomer()
        val invalidCreditCode = -15

        val fakeCredit = Credit(
            customer = fakeCustomer,
            creditValue = BigDecimal.valueOf( 50000.0 ),
            dayFirstInstallment = LocalDate.now().plusDays( 45 ) )

        creditRepository.save( fakeCredit )

        //test
        mockMvc.perform(
            MockMvcRequestBuilders
                .get( "$URL/$invalidCreditCode?customerId=${ fakeCustomer.id }" )
                .accept( "application/json" )
        )
        .andExpect( MockMvcResultMatchers.status().isBadRequest )
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.title")
                .value("Bad Request! Consult the documentation")
        )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.timestamp" ).exists() )
        .andExpect( MockMvcResultMatchers.jsonPath( "$.status" ).value( 400 ) )
        .andExpect(
            MockMvcResultMatchers.jsonPath( "$.exception" )
                .value( "class java.lang.IllegalArgumentException" )
        )
        .andExpect(
            MockMvcResultMatchers
                .jsonPath( "$.details[*]" )
                .value( "Invalid UUID string: $invalidCreditCode" )
        )

        //output
        .andDo( MockMvcResultHandlers.print() )
    }

    @Test
    fun `should not find credit with a customerId mismatching the provided one then return 409 status`() {
        //input
        val fakeCustomer = buildFakeCustomer()

        val fakeCredit = Credit(
            customer = fakeCustomer,
            creditValue = BigDecimal.valueOf( 50000.0 ),
            dayFirstInstallment = LocalDate.now().plusDays( 45 ) )

        val creditCode = creditRepository.save( fakeCredit ).creditCode
        val invalidCustomerId = 10201

        //test
        mockMvc.perform(
            MockMvcRequestBuilders
                .get( "$URL/$creditCode?customerId=${ invalidCustomerId }" )
                .accept( "application/json" )
        )
            .andExpect( MockMvcResultMatchers.status().isBadRequest )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.title")
                    .value("Bad Request! Consult the documentation")
            )
            .andExpect( MockMvcResultMatchers.jsonPath( "$.timestamp" ).exists() )
            .andExpect( MockMvcResultMatchers.jsonPath( "$.status" ).value( 400 ) )
            .andExpect(
                MockMvcResultMatchers.jsonPath( "$.exception" )
                    .value( "class java.lang.IllegalArgumentException" )
            )
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath( "$.details[*]" )
                    .value( "Contact admin" )
            )

            //output
            .andDo( MockMvcResultHandlers.print() )
    }

    private fun buildFakeCustomer(): Customer {
        val customer = CustomerDto(
            "Cami",
            "Cavalcante",
            "28475934625",
            BigDecimal.valueOf(1000.0),
            "camila@email.com",
            "1234",
            "000000",
            "Rua da Cami, 123",
        ).toEntity()

        return customerRepository.save( customer )
    }

    private fun mockCredit(
        creditValue: BigDecimal,
        numberOfInstallments: Int,
        customerId: Long = buildFakeCustomer().id ?: 0L,
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusDays( 15 )
    ): String =
        objectMapper.writeValueAsString(
            CreditDto( creditValue, dayFirstOfInstallment, numberOfInstallments, customerId )
        )

}