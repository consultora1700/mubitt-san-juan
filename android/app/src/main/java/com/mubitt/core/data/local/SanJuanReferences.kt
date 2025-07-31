package com.mubitt.core.data.local

import com.mubitt.core.domain.model.Location

/**
 * Referencias locales específicas de San Juan, Argentina
 * Para búsqueda inteligente de ubicaciones con referencias cotidianas
 */
object SanJuanReferences {
    
    /**
     * Puntos de referencia principales de San Juan
     */
    val MAIN_LANDMARKS = listOf(
        SanJuanLandmark(
            id = "hospital_rawson",
            name = "Hospital Rawson",
            aliases = listOf("hospital", "rawson", "hospital público"),
            location = Location(-31.5375, -68.5364, "Av. Rawson 1005, San Juan", "Hospital Rawson"),
            category = "hospital",
            description = "Hospital público principal de San Juan"
        ),
        SanJuanLandmark(
            id = "unsj_campus",
            name = "Universidad Nacional de San Juan",
            aliases = listOf("unsj", "universidad", "uni", "campus universitario"),
            location = Location(-31.5653, -68.5311, "Av. Ignacio de la Roza 590, San Juan", "UNSJ Campus"),
            category = "university",
            description = "Campus principal de la UNSJ"
        ),
        SanJuanLandmark(
            id = "shopping_del_sol",
            name = "Shopping del Sol",
            aliases = listOf("shopping", "del sol", "centro comercial"),
            location = Location(-31.5244, -68.5289, "Av. José Ignacio de la Roza 8532, San Juan", "Shopping del Sol"),
            category = "shopping",
            description = "Principal centro comercial de San Juan"
        ),
        SanJuanLandmark(
            id = "terminal_bus",
            name = "Terminal de Ómnibus",
            aliases = listOf("terminal", "terminal de buses", "estación de buses"),
            location = Location(-31.5489, -68.5256, "Estados Unidos 1942, San Juan", "Terminal de Ómnibus"),
            category = "transport",
            description = "Terminal principal de buses de San Juan"
        ),
        SanJuanLandmark(
            id = "plaza_25_mayo",
            name = "Plaza 25 de Mayo",
            aliases = listOf("plaza principal", "plaza central", "centro"),
            location = Location(-31.5375, -68.5364, "Plaza 25 de Mayo, San Juan", "Plaza 25 de Mayo"),
            category = "plaza",
            description = "Plaza principal del centro de San Juan"
        ),
        SanJuanLandmark(
            id = "aeropuerto",
            name = "Aeropuerto Domingo Faustino Sarmiento",
            aliases = listOf("aeropuerto", "aeropuerto san juan", "sarmiento"),
            location = Location(-31.5714, -68.4181, "Las Chacritas, San Juan", "Aeropuerto San Juan"),
            category = "airport",
            description = "Aeropuerto principal de San Juan"
        )
    )
    
    /**
     * Barrios y zonas de San Juan
     */
    val NEIGHBORHOODS = listOf(
        SanJuanNeighborhood(
            id = "centro",
            name = "Centro",
            aliases = listOf("centro", "microcentro", "downtown"),
            bounds = LocationBounds(
                north = -31.530,
                south = -31.545,
                east = -68.520,
                west = -68.545
            ),
            description = "Centro histórico y comercial de San Juan"
        ),
        SanJuanNeighborhood(
            id = "desamparados",
            name = "Desamparados",
            aliases = listOf("desamparados", "desam"),
            bounds = LocationBounds(
                north = -31.520,
                south = -31.580,
                east = -68.500,
                west = -68.560
            ),
            description = "Departamento Desamparados"
        ),
        SanJuanNeighborhood(
            id = "rivadavia",
            name = "Rivadavia",
            aliases = listOf("rivadavia", "riva"),
            bounds = LocationBounds(
                north = -31.510,
                south = -31.570,
                east = -68.480,
                west = -68.540
            ),
            description = "Departamento Rivadavia"
        ),
        SanJuanNeighborhood(
            id = "chimbas",
            name = "Chimbas",
            aliases = listOf("chimbas"),
            bounds = LocationBounds(
                north = -31.450,
                south = -31.520,
                east = -68.480,
                west = -68.550
            ),
            description = "Departamento Chimbas"
        )
    )
    
    /**
     * Referencias coloquiales típicas de San Juan
     */
    val COLLOQUIAL_REFERENCES = listOf(
        ColloquialReference(
            phrase = "cerca del semáforo",
            description = "Intersección principal con semáforo",
            searchHints = listOf("semaforo", "esquina principal", "cruce")
        ),
        ColloquialReference(
            phrase = "por la rotonda",
            description = "Cerca de una rotonda o glorieta",
            searchHints = listOf("rotonda", "glorieta", "círculo")
        ),
        ColloquialReference(
            phrase = "al lado del shopping",
            description = "Zona comercial cerca del Shopping del Sol",
            searchHints = listOf("shopping", "centro comercial", "del sol")
        ),
        ColloquialReference(
            phrase = "zona universitaria",
            description = "Área cercana a la UNSJ",
            searchHints = listOf("universidad", "unsj", "estudiantes")
        ),
        ColloquialReference(
            phrase = "por el hospital",
            description = "Zona del Hospital Rawson",
            searchHints = listOf("hospital", "rawson", "salud")
        )
    )
    
    /**
     * Busca landmarks por texto de entrada
     */
    fun searchLandmarks(query: String): List<SanJuanLandmark> {
        val normalizedQuery = query.lowercase().trim()
        return MAIN_LANDMARKS.filter { landmark ->
            landmark.name.lowercase().contains(normalizedQuery) ||
            landmark.aliases.any { it.lowercase().contains(normalizedQuery) } ||
            landmark.description.lowercase().contains(normalizedQuery)
        }
    }
    
    /**
     * Busca barrios por texto de entrada
     */
    fun searchNeighborhoods(query: String): List<SanJuanNeighborhood> {
        val normalizedQuery = query.lowercase().trim()
        return NEIGHBORHOODS.filter { neighborhood ->
            neighborhood.name.lowercase().contains(normalizedQuery) ||
            neighborhood.aliases.any { it.lowercase().contains(normalizedQuery) }
        }
    }
    
    /**
     * Busca referencias coloquiales
     */
    fun searchColloquialReferences(query: String): List<ColloquialReference> {
        val normalizedQuery = query.lowercase().trim()
        return COLLOQUIAL_REFERENCES.filter { reference ->
            reference.phrase.lowercase().contains(normalizedQuery) ||
            reference.searchHints.any { it.lowercase().contains(normalizedQuery) }
        }
    }
}

data class SanJuanLandmark(
    val id: String,
    val name: String,
    val aliases: List<String>,
    val location: Location,
    val category: String,
    val description: String
)

data class SanJuanNeighborhood(
    val id: String,
    val name: String,
    val aliases: List<String>,
    val bounds: LocationBounds,
    val description: String
)

data class LocationBounds(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
)

data class ColloquialReference(
    val phrase: String,
    val description: String,
    val searchHints: List<String>
)