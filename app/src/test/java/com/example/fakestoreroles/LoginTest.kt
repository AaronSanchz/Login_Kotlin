package com.example.fakestoreroles

import org.junit.Assert.*
import org.junit.Test

class LoginTest {
    @Test fun controllerRejectsInvalidCredentialsBeforeTransport() {
        var calls = 0
        val controller = LoginController(StoreTransport { _, _, _ -> calls++; "{}" })
        for ((user, password) in listOf(" " to "pass", "user" to " ", "u".repeat(101) to "pass")) {
            assertThrows(ApiException::class.java) { controller.authenticate(user, password) }
        }
        assertEquals(0, calls)
    }
    @Test fun controllerTrimsUserAndPreservesPassword() {
        val controller = LoginController(StoreTransport { _, path, body ->
            if (path == "/auth/login") {
                assertEquals("mor_2314", body?.getString("username"))
                assertEquals("83r5^_", body?.getString("password"))
                """{"token":"ok"}"""
            } else """[{"id":2,"username":"mor_2314"}]"""
        })
        assertEquals(UserRole.ADMINISTRADOR, controller.authenticate(" mor_2314 ", "83r5^_").role)
    }
    @Test fun successfulLoginUsesApiAndLocalRole() {
        val calls = mutableListOf<String>()
        val result = ApiService.authenticate("mor_2314", "83r5^_", StoreTransport { method, path, body ->
            calls.add("$method $path")
            if (path == "/auth/login") {
                assertEquals("mor_2314", body!!.getString("username"))
                """{"token":"valid-token"}"""
            } else """[{"id":2,"username":"mor_2314"}]"""
        })
        assertEquals(UserRole.ADMINISTRADOR, result.role)
        assertEquals(listOf("POST /auth/login", "GET /users"), calls)
    }
    @Test fun invalidInputsDoNotUseNetwork() {
        var calls = 0
        val transport = StoreTransport { _, _, _ -> calls++; "{}" }
        for ((u,p) in listOf("" to "pass", "user" to " ", "u".repeat(101) to "p", "u" to "p".repeat(257))) {
            assertThrows(ApiException::class.java) { ApiService.authenticate(u,p,transport) }
        }
        assertEquals(0,calls)
    }
    @Test fun missingOrMalformedTokenCannotCreateSession() {
        for (response in listOf("{}", "null", "<html>error</html>", """{"token":" "}""")) {
            assertThrows(ApiException::class.java) {
                ApiService.authenticate("user","pass",StoreTransport { _,_,_ -> response })
            }
        }
    }
    @Test fun unknownUserCannotCreateSession() {
        assertThrows(ApiException::class.java) {
            ApiService.authenticate("user","pass",StoreTransport { _,path,_ -> if(path == "/auth/login") """{"token":"ok"}""" else "[]" })
        }
    }
    @Test fun roleMappingAndFieldLimits() {
        assertEquals(UserRole.ADMINISTRADOR,ApiService.roleFromId(1))
        assertEquals(UserRole.AUDITOR,ApiService.roleFromId(3))
        assertEquals(UserRole.CLIENTE,ApiService.roleFromId(4))
        assertNotNull(LoginRules.username(" "))
        assertNotNull(LoginRules.password("p".repeat(257)))
        assertNull(LoginRules.password("83r5^_"))
    }
}
