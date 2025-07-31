package com.mubitt.core.domain.model

/**
 * Referencias específicas de San Juan para búsqueda inteligente
 */
data class SanJuanReference(
    val id: String,
    val name: String,
    val commonNames: List<String>, // Nombres alternativos que usa la gente
    val category: ReferenceCategory,
    val location: Location,
    val description: String,
    val isPopular: Boolean = false,
    val searchKeywords: List<String> = emptyList()
)

enum class ReferenceCategory {
    HOSPITAL,
    UNIVERSITY,
    SHOPPING,
    GOVERNMENT,
    TRANSPORT,
    LANDMARK,
    SCHOOL,
    BANK,
    RESTAURANT,
    GAS_STATION
}

/**
 * Referencias coloquiales que usan los sanjuaninos
 */
object SanJuanReferences {
    
    val POPULAR_REFERENCES = listOf(
        SanJuanReference(
            id = "hospital_rawson",
            name = "Hospital Rawson",
            commonNames = listOf(
                "Rawson",
                "Hospital",
                "Hospital público", 
                "El hospital"
            ),
            category = ReferenceCategory.HOSPITAL,
            location = Location(
                latitude = -31.5375,
                longitude = -68.5364,
                address = "Av. Ignacio de la Roza 130, San Juan"
            ),
            description = "Hospital público más importante de San Juan",
            isPopular = true,
            searchKeywords = listOf("emergencia", "doctor", "médico", "salud")
        ),
        
        SanJuanReference(
            id = "unsj",
            name = "Universidad Nacional de San Juan",
            commonNames = listOf(
                "UNSJ",
                "Universidad",
                "La universidad",
                "Campus",
                "Facultad"
            ),
            category = ReferenceCategory.UNIVERSITY,
            location = Location(
                latitude = -31.5441,
                longitude = -68.5504,
                address = "Av. Libertador San Martín, San Juan"
            ),
            description = "Universidad Nacional de San Juan - Campus principal",
            isPopular = true,
            searchKeywords = listOf("estudiante", "facultad", "carrera", "clases")
        ),
        
        SanJuanReference(
            id = "plaza_25",
            name = "Plaza 25 de Mayo",
            commonNames = listOf(
                "Plaza",
                "Plaza principal",
                "Centro",
                "Plaza 25",
                "La plaza"
            ),
            category = ReferenceCategory.LANDMARK,
            location = Location(
                latitude = -31.5375,
                longitude = -68.5289,
                address = "Plaza 25 de Mayo, Centro, San Juan"
            ),
            description = "Plaza principal del centro de San Juan",
            isPopular = true,
            searchKeywords = listOf("centro", "principal", "histórica")
        ),
        
        SanJuanReference(
            id = "shopping_del_sol",
            name = "Shopping del Sol",
            commonNames = listOf(
                "Shopping",
                "Mall",
                "Shopping del Sol",
                "Centro comercial",
                "Del Sol"
            ),
            category = ReferenceCategory.SHOPPING,
            location = Location(
                latitude = -31.5203,
                longitude = -68.5289,
                address = "Av. José Ignacio de la Roza, San Juan"
            ),
            description = "Centro comercial más grande de San Juan",
            isPopular = true,
            searchKeywords = listOf("compras", "cine", "comida", "tiendas")
        ),
        
        SanJuanReference(
            id = "terminal_omnibus",
            name = "Terminal de Ómnibus",
            commonNames = listOf(
                "Terminal",
                "Estación de buses",
                "Terminal de buses",
                "Ómnibus"
            ),
            category = ReferenceCategory.TRANSPORT,
            location = Location(
                latitude = -31.5344,
                longitude = -68.5197,
                address = "Estados Unidos, San Juan"
            ),
            description = "Terminal principal de ómnibus de San Juan",
            isPopular = true,
            searchKeywords = listOf("viaje", "bus", "transporte", "salir")
        ),
        
        SanJuanReference(
            id = "casa_gobierno",
            name = "Casa de Gobierno",
            commonNames = listOf(
                "Gobierno",
                "Casa de Gobierno",
                "Gobernación",
                "Edificio gobierno"
            ),
            category = ReferenceCategory.GOVERNMENT,
            location = Location(
                latitude = -31.5353,
                longitude = -68.5292,
                address = "Salta, San Juan"
            ),
            description = "Sede del gobierno provincial",
            isPopular = true,
            searchKeywords = listOf("trámite", "gobierno", "provincial")
        ),
        
        SanJuanReference(
            id = "estadio_bicentenario",
            name = "Estadio del Bicentenario",
            commonNames = listOf(
                "Estadio",
                "Bicentenario",
                "Cancha",
                "Estadio San Juan"
            ),
            category = ReferenceCategory.LANDMARK,
            location = Location(
                latitude = -31.5681,
                longitude = -68.5756,
                address = "Pocito, San Juan"
            ),
            description = "Estadio principal de San Juan",
            isPopular = true,
            searchKeywords = listOf("fútbol", "partido", "deporte", "evento")
        ),
        
        SanJuanReference(
            id = "aeropuerto",
            name = "Aeropuerto Domingo Faustino Sarmiento",
            commonNames = listOf(
                "Aeropuerto",
                "Aeropuerto San Juan",
                "Vuelo",
                "Terminal aérea"
            ),
            category = ReferenceCategory.TRANSPORT,
            location = Location(
                latitude = -31.5714,
                longitude = -68.4182,
                address = "Pocito, San Juan"
            ),
            description = "Aeropuerto de San Juan",
            isPopular = true,
            searchKeywords = listOf("vuelo", "avión", "viaje", "aerolínea")
        )
    )
    
    /**
     * Patrones de búsqueda comunes en San Juan
     */
    val SEARCH_PATTERNS = mapOf(
        // Patrones de ubicación relativa
        "cerca del" to "reference_search",
        "al lado de" to "reference_search", 
        "frente a" to "reference_search",
        "por el" to "reference_search",
        "en el" to "reference_search",
        
        // Patrones de transporte
        "parada" to "transport_search",
        "estación" to "transport_search",
        "semáforo" to "landmark_search",
        "rotonda" to "landmark_search",
        
        // Patrones comerciales
        "farmacia" to "commercial_search",
        "supermercado" to "commercial_search",
        "banco" to "commercial_search",
        "cajero" to "commercial_search"
    )
}