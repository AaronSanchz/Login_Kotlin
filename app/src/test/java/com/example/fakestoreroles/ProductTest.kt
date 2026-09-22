package com.example.fakestoreroles

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class ProductTest {
    private val json = """{"id":1,"title":"Camisa","price":19.5,"description":"Algodón","category":"men's clothing","image":"https://example.com/a.png","rating":{"rate":4.2,"count":9}}"""
    private fun session(role: UserRole) = SessionData("test",1,"test",role)
    @Test fun mapsAllFields() { val p = Product.fromJson(JSONObject(json)); assertEquals(9,p.rating?.count);assertEquals("Algodón",p.description) }
    @Test fun rejectsInvalidRequiredValues() {
        for ((key,value) in listOf("id" to 0,"id" to 1.2,"price" to -1,"title" to " ","description" to JSONObject.NULL,"category" to 42)) {
            assertThrows(ApiException::class.java) { Product.fromJson(JSONObject(json).put(key,value)) }
        }
    }
    @Test fun optionalDataHasFallback() {
        val p = Product.fromJson(JSONObject(json).put("image",JSONObject.NULL).put("rating",JSONObject.NULL))
        assertEquals("",p.image);assertNull(p.rating)
    }
    @Test fun validatesUserInputs() {
        listOf("", " ", "-1", "0", "NaN", "Infinity", "1e3", "1.234", "1000001").forEach { assertNotNull(ProductRules.price(it)) }
        assertNull(ProductRules.price("12,50"));assertEquals(12.5,ProductRules.parsePrice("12,50")!!,0.001)
        assertNotNull(ProductRules.text("  "));assertNotNull(ProductRules.image("http://example.com/a"));assertNull(ProductRules.image("https://example.com/a"))
    }
    @Test fun routesAreCorrectAndCategoryIsEncoded() {
        val paths = mutableListOf<String>()
        val repo = HttpProductRepository({null},StoreTransport { _,path,_ ->
            paths.add(path)
            when(path) { "/products/categories" -> "[\"men's clothing\"]"; "/products/1" -> json; else -> "[$json]" }
        })
        repo.products();repo.categories();repo.products("men's clothing");repo.detail(1)
        assertEquals(listOf("/products","/products/categories","/products/category/men%27s%20clothing","/products/1"),paths)
    }
    @Test fun malformedAndNullResponsesAreErrors() {
        listOf("", "null", "broken", "{}").forEach { response ->
            val repo = HttpProductRepository({null},StoreTransport { _,_,_ -> response })
            assertThrows(ApiException::class.java) { repo.products() }
        }
    }
    @Test fun emptyListIsValid() { assertTrue(HttpProductRepository({null},StoreTransport { _,_,_ -> "[]" }).products().isEmpty()) }
    @Test fun wrongDetailIdIsRejected() {
        val repo = HttpProductRepository({null},StoreTransport { _,_,_ -> JSONObject(json).put("id",2).toString() })
        assertThrows(ApiException::class.java) { repo.detail(1) }
    }
    @Test fun readOnlyRolesCannotSendWrites() {
        listOf(UserRole.CLIENTE,UserRole.AUDITOR,null).forEach { role ->
            var calls=0
            val repo = HttpProductRepository({role?.let { session(it) }},StoreTransport { _,_,_ -> calls++;json })
            assertThrows(ApiException::class.java) { repo.update(Product.fromJson(JSONObject(json)),listOf("men's clothing")) }
            assertThrows(ApiException::class.java) { repo.delete(1) }
            assertEquals(0,calls)
        }
    }
    @Test fun adminWritesUsePutAndDelete() {
        val methods=mutableListOf<String>()
        val repo = HttpProductRepository({session(UserRole.ADMINISTRADOR)},StoreTransport { method,_,body ->
            methods.add(method);if(method=="PUT")assertEquals("Camisa",body?.getString("title"));json
        })
        repo.update(Product.fromJson(JSONObject(json)),listOf("men's clothing"));repo.delete(1)
        assertEquals(listOf("PUT","DELETE"),methods)
    }
    @Test fun invalidMutationDoesNotSendRequest() {
        var calls=0
        val repo = HttpProductRepository({session(UserRole.ADMINISTRADOR)},StoreTransport { _,_,_ -> calls++;json })
        assertThrows(ApiException::class.java) { repo.update(Product.fromJson(JSONObject(json)).copy(title=""),listOf("men's clothing")) }
        assertEquals(0,calls)
    }
}
