package com.example.fakestoreroles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    class Fake : ProductRepository {
        var fail=false
        val calls=mutableListOf<String?>()
        override fun products(category: String?): List<Product> {
            calls.add(category)
            if(fail) throw ApiException("Sin red")
            return listOf(Product(1,"Camisa",2.0,"Descripción",category ?: "ropa",""))
        }
        override fun categories()=listOf("ropa","joyas")
        override fun detail(id:Int)=throw UnsupportedOperationException()
        override fun update(product:Product,categories:List<String>)=throw UnsupportedOperationException()
        override fun delete(id:Int)=Unit
    }
    @Test fun loadingFilteringRetryAndReset()=runTest {
        val dispatcher=StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repo=Fake();val vm=CatalogViewModel(repo,dispatcher)
            assertTrue(vm.state.value.loading);advanceUntilIdle();assertEquals(1,vm.state.value.products.size)
            vm.load("joyas");assertTrue(vm.state.value.products.isEmpty());assertTrue(vm.state.value.loading)
            advanceUntilIdle();assertEquals("joyas",vm.state.value.products.single().category)
            repo.fail=true;vm.load();advanceUntilIdle();assertEquals("Sin red",vm.state.value.error);assertFalse(vm.state.value.loading)
            repo.fail=false;vm.load();advanceUntilIdle();assertNull(vm.state.value.error);assertNull(vm.state.value.selected)
            vm.load("desconocida");advanceUntilIdle();assertNull(vm.state.value.selected)
        } finally { Dispatchers.resetMain() }
    }
}
