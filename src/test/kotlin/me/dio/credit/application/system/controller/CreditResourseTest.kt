package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.repository.CreditRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourseTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

    @Test
    fun `should save a credit request then return 201 status`(): Unit =
        TODO( "return 201 status after saving a credit request" )

    @Test
    fun `should not save a credit with dayFirstInstallment longer than 3 months then return 400 status`(): Unit =
        TODO( "return 400 status if dayFirstInstallment longer than 3 months" )

    @Test
    fun `should not save a credit with invalid numberOfInstallments then return 400 status`(): Unit =
        TODO( "then return 400 status if numberOfInstallments is invalid" )

    @Test
    fun `should find a credit request by a credit code then return 200 status`(): Unit =
        TODO( "return 200 status after finding a credit request by a credit code" )

    @Test
    fun `should not find credit with a invalid creditCode then return 400 status`(): Unit =
        TODO( "return 400 status if creditCode is invalid" )

    @Test
    fun `should not find credit with a customerId mismatching the provided one then return 409 status`(): Unit =
        TODO( "return 409 status if a customerId mismatches the provided one" )
}