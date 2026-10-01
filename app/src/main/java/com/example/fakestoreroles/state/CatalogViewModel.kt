// GUÍA DEL ARCHIVO: Estado CatalogState expuesto como StateFlow de solo lectura. load limpia datos y cancela el Job anterior; generation evita respuestas obsoletas. viewModelScope y Dispatchers.IO separan ciclo de vida y trabajo HTTP.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Estado, filtros y concurrencia de las consultas del catálogo. */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Agrupa productos, categorías, selección, carga y errores. */
data class CatalogState(
    val products: List<Product> = emptyList(), val categories: List<String> = emptyList(),
    val selected: String? = null, val loading: Boolean = false,
    val categoriesLoading: Boolean = false, val error: String? = null, val categoryError: String? = null
)

/** Conserva el catálogo al rotar; las solicitudes antiguas no reemplazan la selección nueva. */
class CatalogViewModel(private val repository: ProductRepository, private val io: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val mutable = MutableStateFlow(CatalogState())
    val state = mutable.asStateFlow()
    private var productsJob: Job? = null
    private var generation = 0
    init { load(); loadCategories() }
    /** Recibe categoría opcional; limpia lista, marca carga y acepta solo la respuesta de la generación actual. */
    fun load(category: String? = null) {
        if (category != null && category !in mutable.value.categories) return
        val request = ++generation
        productsJob?.cancel()
        mutable.value = mutable.value.copy(products = emptyList(), selected = category, loading = true, error = null)
        productsJob = viewModelScope.launch {
            try {
                val products = withContext(io) { repository.products(category) }
                if (generation == request) mutable.value = mutable.value.copy(products = products, loading = false)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (generation == request) mutable.value = mutable.value.copy(loading = false, error = e.message ?: "No se pudo cargar el catálogo.")
            }
        }
    }
    /** Consulta categorías con indicador y error independientes; evita solicitudes simultáneas duplicadas. */
    fun loadCategories() {
        if (mutable.value.categoriesLoading) return
        mutable.value = mutable.value.copy(categoriesLoading = true, categoryError = null)
        viewModelScope.launch {
            try {
                val result = withContext(io) { repository.categories() }
                mutable.value = mutable.value.copy(categories = result, categoriesLoading = false)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                mutable.value = mutable.value.copy(categoriesLoading = false, categoryError = "No se pudieron cargar las categorías.")
            }
        }
    }
}
