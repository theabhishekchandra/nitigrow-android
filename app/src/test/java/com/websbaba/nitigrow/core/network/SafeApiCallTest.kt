package com.websbaba.nitigrow.core.network

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SafeApiCallTest {

    @Test
    fun `a payload the DTOs cannot read becomes a friendly parsing error, never raw exception text`() = runBlocking {
        val result = safeApiCall(Dispatchers.Unconfined) {
            throw JsonSyntaxException("java.lang.IllegalStateException: Expected BEGIN_OBJECT but was BEGIN_ARRAY")
        }

        val error = result as ApiResult.Error
        assertThat(error.type).isEqualTo(ErrorType.Parsing)
        assertThat(error.message).isEqualTo(UNREADABLE_RESPONSE)
        assertThat(error.message).doesNotContain("BEGIN_OBJECT")
    }

    @Test
    fun `an unexpected exception is reported generically`() = runBlocking {
        val result = safeApiCall(Dispatchers.Unconfined) { error("boom: internal detail") }

        val error = result as ApiResult.Error
        assertThat(error.message).isEqualTo(GENERIC_ERROR)
    }

    @Test
    fun `andThen hands the payload to the transform`() = runBlocking {
        val result = safeApiCall(Dispatchers.Unconfined) { 20 }.andThen { ApiResult.Success(it + 1) }

        assertThat(result).isEqualTo(ApiResult.Success(21))
    }

    @Test
    fun `andThen turns a mapper crash into an error instead of throwing`() = runBlocking {
        val result = safeApiCall(Dispatchers.Unconfined) { listOf<String?>("a", null) }.andThen { rows ->
            // e.g. a mapper calling a non-null accessor on a field the server omitted
            ApiResult.Success(rows.map { it!!.length })
        }

        val error = result as ApiResult.Error
        assertThat(error.type).isEqualTo(ErrorType.Parsing)
    }

    @Test
    fun `andThen leaves an upstream error untouched`() = runBlocking {
        val upstream = safeApiCall(Dispatchers.Unconfined) { throw java.io.IOException("offline") }

        val result = upstream.andThen { _: Nothing -> ApiResult.Success(Unit) }

        assertThat((result as ApiResult.Error).type).isEqualTo(ErrorType.Network)
    }
}
