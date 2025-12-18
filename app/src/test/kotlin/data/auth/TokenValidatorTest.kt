package data.auth

import core.model.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenValidatorTest {

    @Test
    fun `returns true on 200`() = runBlocking {
        val engine = mockEngine(HttpStatusCode.OK)
        val validator = TokenValidator(HttpClient(engine))

        val result = validator.validate("dummy")

        assertTrue((result as Result.Success).value)
    }

    @Test
    fun `returns false on 401`() = runBlocking {
        val engine = mockEngine(HttpStatusCode.Unauthorized)
        val validator = TokenValidator(HttpClient(engine))

        val result = validator.validate("invalid")

        assertEquals(false, (result as Result.Success).value)
    }

    @Test
    fun `returns failure on unexpected status`() = runBlocking {
        val engine = mockEngine(HttpStatusCode.BadGateway)
        val validator = TokenValidator(HttpClient(engine))

        val result = validator.validate("token")

        assertTrue(result is Result.Failure)
    }

    private fun mockEngine(status: HttpStatusCode) = MockEngine { request: HttpRequestData ->
        respondFor(request, status)
    }

    private fun MockRequestHandleScope.respondFor(request: HttpRequestData, status: HttpStatusCode) =
        if (request.headers[HttpHeaders.Authorization].isNullOrBlank()) {
            respond("", HttpStatusCode.Forbidden)
        } else {
            respond("{}", status)
        }
}
