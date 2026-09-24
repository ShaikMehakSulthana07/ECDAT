package com.ecdat.backend.scanner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoScannerCipherTransformationTest {

    private List<CryptoFinding> scanCode(String codeSnippet) {
        CompilationUnit cu = StaticJavaParser.parse(codeSnippet);
        CryptoScanner scanner = new CryptoScanner("TestFile.java");
        List<CryptoFinding> findings = new ArrayList<>();
        scanner.visit(cu, findings);
        return findings;
    }

    @Test
    public void testTransformationWithAllThreeParts() {
        String code = "class Test { void f() throws Exception { javax.crypto.Cipher.getInstance(\"AES/GCM/NoPadding\"); } }";
        List<CryptoFinding> findings = scanCode(code);

        assertEquals(1, findings.size());
        CryptoFinding f = findings.get(0);
        assertEquals("AES", f.getAlgorithm());
        assertEquals("GCM", f.getMode());
        assertEquals("NoPadding", f.getPadding());
        assertEquals("GCM", f.getVariant());
        assertEquals(CryptoFinding.Purpose.ENCRYPTION, f.getPurpose());
        assertEquals(CryptoFinding.Confidence.HIGH, f.getConfidence());
    }

    @Test
    public void testTransformationWithAlgorithmOnly() {
        String code = "class Test { void f() throws Exception { javax.crypto.Cipher.getInstance(\"AES\"); } }";
        List<CryptoFinding> findings = scanCode(code);

        assertEquals(1, findings.size());
        CryptoFinding f = findings.get(0);
        assertEquals("AES", f.getAlgorithm());
        assertNull(f.getMode(), "Mode should be null when only algorithm is specified");
        assertNull(f.getPadding(), "Padding should be null when only algorithm is specified");
        assertEquals("AES", f.getVariant());
        assertEquals(CryptoFinding.Purpose.ENCRYPTION, f.getPurpose());
    }

    @Test
    public void testTransformationWithAlgorithmAndModeOnly() {
        String code = "class Test { void f() throws Exception { javax.crypto.Cipher.getInstance(\"AES/CBC\"); } }";
        List<CryptoFinding> findings = scanCode(code);

        assertEquals(1, findings.size());
        CryptoFinding f = findings.get(0);
        assertEquals("AES", f.getAlgorithm());
        assertEquals("CBC", f.getMode());
        assertNull(f.getPadding(), "Padding should be null when no padding part is in transformation");
        assertEquals("CBC", f.getVariant());
        assertEquals(CryptoFinding.Purpose.ENCRYPTION, f.getPurpose());
    }

    @Test
    public void testRSATransformationWithPadding() {
        String code = "class Test { void f() throws Exception { javax.crypto.Cipher.getInstance(\"RSA/ECB/PKCS1Padding\"); } }";
        List<CryptoFinding> findings = scanCode(code);

        assertEquals(1, findings.size());
        CryptoFinding f = findings.get(0);
        assertEquals("RSA", f.getAlgorithm());
        assertEquals("ECB", f.getMode());
        assertEquals("PKCS1Padding", f.getPadding());
        assertEquals("RSA/ECB/PKCS1Padding", f.getVariant());
        assertEquals(CryptoFinding.Purpose.ENCRYPTION, f.getPurpose());
    }
}
