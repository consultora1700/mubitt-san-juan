package com.mubitt.core.domain.usecase

import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.SanJuanReference
import com.mubitt.core.domain.model.SanJuanReferences
import javax.inject.Inject

/**
 * Use case para búsqueda inteligente de ubicaciones en San Juan
 * Entiende referencias locales y patrones de búsqueda coloquiales
 */
class SearchSanJuanLocationUseCase @Inject constructor() {
    
    /**
     * Busca ubicaciones usando referencias locales y patrones sanjuaninos
     */
    fun searchLocation(query: String): List<SearchResult> {
        val cleanQuery = query.trim().lowercase()
        val results = mutableListOf<SearchResult>()
        
        // 1. Búsqueda exacta por nombre o nombre común
        results.addAll(searchByExactMatch(cleanQuery))
        
        // 2. Búsqueda por patrones (cerca del, al lado de, etc.)
        results.addAll(searchByPattern(cleanQuery))
        
        // 3. Búsqueda por keywords
        results.addAll(searchByKeywords(cleanQuery))
        
        // 4. Búsqueda fuzzy para errores de tipeo
        results.addAll(searchFuzzy(cleanQuery))
        
        return results
            .distinctBy { it.reference.id }
            .sortedByDescending { it.relevanceScore }
            .take(10)
    }
    
    private fun searchByExactMatch(query: String): List<SearchResult> {
        return SanJuanReferences.POPULAR_REFERENCES.mapNotNull { reference ->
            val score = calculateExactMatchScore(query, reference)
            if (score > 0) {
                SearchResult(
                    reference = reference,
                    relevanceScore = score,
                    matchType = MatchType.EXACT_MATCH,
                    matchedText = findBestMatch(query, reference)
                )
            } else null
        }
    }
    
    private fun searchByPattern(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        SanJuanReferences.SEARCH_PATTERNS.forEach { (pattern, type) ->
            if (query.contains(pattern)) {
                val remainingQuery = query.replace(pattern, "").trim()
                
                // Buscar la referencia en el texto restante
                SanJuanReferences.POPULAR_REFERENCES.forEach { reference ->
                    if (matchesReference(remainingQuery, reference)) {
                        results.add(
                            SearchResult(
                                reference = reference,
                                relevanceScore = 0.8f,
                                matchType = MatchType.PATTERN_MATCH,
                                matchedText = "$pattern ${reference.name}"
                            )
                        )
                    }
                }
            }
        }
        
        return results
    }
    
    private fun searchByKeywords(query: String): List<SearchResult> {
        return SanJuanReferences.POPULAR_REFERENCES.mapNotNull { reference ->
            val keywordScore = reference.searchKeywords.maxOfOrNull { keyword ->
                if (query.contains(keyword)) 0.6f else 0f
            } ?: 0f
            
            if (keywordScore > 0) {
                SearchResult(
                    reference = reference,
                    relevanceScore = keywordScore,
                    matchType = MatchType.KEYWORD_MATCH,
                    matchedText = reference.name
                )
            } else null
        }
    }
    
    private fun searchFuzzy(query: String): List<SearchResult> {
        return SanJuanReferences.POPULAR_REFERENCES.mapNotNull { reference ->
            val fuzzyScore = calculateFuzzyScore(query, reference)
            if (fuzzyScore > 0.4f) { // Umbral mínimo para fuzzy matching
                SearchResult(
                    reference = reference,
                    relevanceScore = fuzzyScore * 0.5f, // Score reducido para fuzzy
                    matchType = MatchType.FUZZY_MATCH,
                    matchedText = reference.name
                )
            } else null
        }
    }
    
    private fun calculateExactMatchScore(query: String, reference: SanJuanReference): Float {
        // Buscar coincidencia exacta con el nombre principal
        if (reference.name.lowercase().contains(query)) {
            return if (reference.name.lowercase() == query) 1.0f else 0.9f
        }
        
        // Buscar en nombres comunes
        reference.commonNames.forEach { commonName ->
            if (commonName.lowercase().contains(query)) {
                return if (commonName.lowercase() == query) 0.95f else 0.8f
            }
        }
        
        return 0f
    }
    
    private fun calculateFuzzyScore(query: String, reference: SanJuanReference): Float {
        val allNames = listOf(reference.name) + reference.commonNames
        
        return allNames.maxOfOrNull { name ->
            val similarity = calculateStringSimilarity(query, name.lowercase())
            if (reference.isPopular) similarity * 1.1f else similarity
        } ?: 0f
    }
    
    private fun calculateStringSimilarity(s1: String, s2: String): Float {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0f
        
        val distance = levenshteinDistance(s1, s2)
        return 1.0f - (distance.toFloat() / maxLength)
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    private fun matchesReference(query: String, reference: SanJuanReference): Boolean {
        val allNames = listOf(reference.name) + reference.commonNames
        return allNames.any { name ->
            name.lowercase().contains(query) || query.contains(name.lowercase())
        }
    }
    
    private fun findBestMatch(query: String, reference: SanJuanReference): String {
        val allNames = listOf(reference.name) + reference.commonNames
        return allNames.firstOrNull { name ->
            name.lowercase().contains(query)
        } ?: reference.name
    }
}

/**
 * Resultado de búsqueda con información de relevancia
 */
data class SearchResult(
    val reference: SanJuanReference,
    val relevanceScore: Float,
    val matchType: MatchType,
    val matchedText: String
)

enum class MatchType {
    EXACT_MATCH,      // Coincidencia exacta
    PATTERN_MATCH,    // "cerca del hospital"
    KEYWORD_MATCH,    // Por palabras clave
    FUZZY_MATCH       // Similaridad de texto
}