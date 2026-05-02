package dev.teogor.traverse.compose.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [TraverseDeepLinkMatcher].
 *
 * Covers pattern compilation and URI matching for the range of patterns expected in
 * production: scheme + host + path params, mid-path params, query params, optional query
 * strings, and non-matching URIs.
 */
class TraverseDeepLinkMatcherTest {

    // ── compile + match — basic single param ──────────────────────────────────

    @Test
    fun match_simpleSingleParam_extractsValue() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        val result = TraverseDeepLinkMatcher.match(compiled, "traverse://demo/target/abc")
        assertNotNull(result)
        assertEquals("abc", result["id"])
    }

    @Test
    fun match_httpsSingleParam_extractsValue() {
        val compiled = TraverseDeepLinkMatcher.compile("https://example.com/user/{userId}")
        val result = TraverseDeepLinkMatcher.match(compiled, "https://example.com/user/42")
        assertNotNull(result)
        assertEquals("42", result["userId"])
    }

    @Test
    fun match_numericId_extractedAsString() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        val result = TraverseDeepLinkMatcher.match(compiled, "traverse://demo/target/99")
        assertNotNull(result)
        assertEquals("99", result["id"])
    }

    // ── multiple params ───────────────────────────────────────────────────────

    @Test
    fun match_twoParams_bothExtracted() {
        val compiled = TraverseDeepLinkMatcher.compile("https://example.com/user/{userId}/post/{postId}")
        val result = TraverseDeepLinkMatcher.match(compiled, "https://example.com/user/42/post/100")
        assertNotNull(result)
        assertEquals("42", result["userId"])
        assertEquals("100", result["postId"])
    }

    // ── query params ──────────────────────────────────────────────────────────

    @Test
    fun match_withTrailingQueryString_allowedAndExtracted() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/search")
        // URI with query — should match (pattern ends with no params, query is optional).
        val result = TraverseDeepLinkMatcher.match(compiled, "traverse://demo/search?q=kotlin")
        assertNotNull(result)
        assertEquals("kotlin", result["q"])
    }

    // ── no match cases ────────────────────────────────────────────────────────

    @Test
    fun match_differentScheme_returnsNull() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        assertNull(TraverseDeepLinkMatcher.match(compiled, "https://demo/target/42"))
    }

    @Test
    fun match_differentHost_returnsNull() {
        val compiled = TraverseDeepLinkMatcher.compile("https://example.com/user/{userId}")
        assertNull(TraverseDeepLinkMatcher.match(compiled, "https://other.com/user/42"))
    }

    @Test
    fun match_differentPath_returnsNull() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        assertNull(TraverseDeepLinkMatcher.match(compiled, "traverse://demo/other/42"))
    }

    @Test
    fun match_emptySegmentWherePlaceholderRequired_returnsNull() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        // Missing the segment value after /target/
        assertNull(TraverseDeepLinkMatcher.match(compiled, "traverse://demo/target/"))
    }

    // ── fragment stripping ────────────────────────────────────────────────────

    @Test
    fun match_uriWithFragment_fragmentIgnored() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/target/{id}")
        val result = TraverseDeepLinkMatcher.match(compiled, "traverse://demo/target/hello#section")
        assertNotNull(result)
        assertEquals("hello", result["id"])
    }

    // ── mid-path parameter ────────────────────────────────────────────────────

    @Test
    fun match_paramInMiddleOfPath_extracted() {
        val compiled = TraverseDeepLinkMatcher.compile("https://example.com/team/{teamId}/member/{memberId}")
        val result = TraverseDeepLinkMatcher.match(compiled, "https://example.com/team/eng/member/alice")
        assertNotNull(result)
        assertEquals("eng", result["teamId"])
        assertEquals("alice", result["memberId"])
    }

    // ── data object pattern (no params) ──────────────────────────────────────

    @Test
    fun match_patternWithNoParams_matchesExactUri() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/home")
        assertNotNull(TraverseDeepLinkMatcher.match(compiled, "traverse://demo/home"))
    }

    @Test
    fun match_patternWithNoParams_extraSegmentDoesNotMatch() {
        val compiled = TraverseDeepLinkMatcher.compile("traverse://demo/home")
        // /home/extra is not allowed — strict match
        assertNull(TraverseDeepLinkMatcher.match(compiled, "traverse://demo/home/extra"))
    }
}

