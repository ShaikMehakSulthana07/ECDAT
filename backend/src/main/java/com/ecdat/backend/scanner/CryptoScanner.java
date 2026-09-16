package com.ecdat.backend.scanner;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Optional;

public class CryptoScanner extends VoidVisitorAdapter<List<CryptoFinding>> {
    private final String filePath;

    public CryptoScanner(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void visit(MethodCallExpr n, List<CryptoFinding> findings) {
        super.visit(n, findings);

        if ("getInstance".equals(n.getNameAsString()) && n.getScope().isPresent()) {
            String scope = n.getScope().get().toString();
            
            if (isCryptoAPI(scope) && n.getArguments().isNonEmpty()) {
                Expression arg = n.getArgument(0);
                CryptoFinding finding = new CryptoFinding();
                finding.setFile(filePath);
                finding.setLine(n.getBegin().map(pos -> pos.line).orElse(-1));
                finding.setEvidence(n.toString());
                finding.setSourceType("JAVA_AST");

                if (arg.isStringLiteralExpr()) {
                    String literalValue = arg.asStringLiteralExpr().getValue();
                    finding.setConfidence(CryptoFinding.Confidence.HIGH);
                    parseAlgorithmDetails(scope, literalValue, finding);
                    
                    if ("RSA".equals(finding.getAlgorithm()) && "KeyPairGenerator".equals(scope)) {
                        extractKeySize(n, finding);
                    }
                } else {
                    finding.setAlgorithm("UNKNOWN");
                    finding.setPurpose(CryptoFinding.Purpose.UNKNOWN);
                    finding.setConfidence(CryptoFinding.Confidence.LOW);
                }
                findings.add(finding);
            }
        }
    }

    private boolean isCryptoAPI(String scope) {
        return List.of("Cipher", "KeyPairGenerator", "Signature", "KeyAgreement", "MessageDigest", 
                       "SSLContext", "KeyFactory", "SecretKeyFactory", "KeyGenerator", "Mac").contains(scope);
    }

    private void parseAlgorithmDetails(String scope, String literalValue, CryptoFinding finding) {
        String upperVal = literalValue.toUpperCase();
        
        switch (scope) {
            case "Cipher":
                finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
                if (upperVal.startsWith("AES")) {
                    finding.setAlgorithm("AES");
                    finding.setVariant(literalValue.contains("/") ? literalValue.split("/")[1] : "AES");
                } else if (upperVal.startsWith("DES")) {
                    finding.setAlgorithm("DES");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("3DES") || upperVal.contains("DESede")) {
                    finding.setAlgorithm("3DES");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("ChaCha20") || upperVal.contains("CHACHA20")) {
                    finding.setAlgorithm("ChaCha20");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("RSA")) {
                    finding.setAlgorithm("RSA");
                    finding.setVariant(literalValue);
                    finding.setPurpose(CryptoFinding.Purpose.ENCRYPTION);
                }
                break;
            case "KeyPairGenerator":
                finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
                if (upperVal.equals("RSA")) {
                    finding.setAlgorithm("RSA");
                    finding.setVariant("RSA");
                } else if (upperVal.contains("EC") || upperVal.contains("EllipticCurve")) {
                    finding.setAlgorithm("EC");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("DSA")) {
                    finding.setAlgorithm("DSA");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("DH")) {
                    finding.setAlgorithm("DH");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("Ed25519")) {
                    finding.setAlgorithm("Ed25519");
                    finding.setVariant(literalValue);
                }
                break;
            case "Signature":
                finding.setPurpose(CryptoFinding.Purpose.DIGITAL_SIGNATURE);
                if (upperVal.contains("ECDSA")) {
                    finding.setAlgorithm("ECDSA");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("RSA")) {
                    finding.setAlgorithm("RSA");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("DSA")) {
                    finding.setAlgorithm("DSA");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("Ed25519")) {
                    finding.setAlgorithm("Ed25519");
                    finding.setVariant(literalValue);
                }
                break;
            case "KeyAgreement":
                finding.setPurpose(CryptoFinding.Purpose.KEY_AGREEMENT);
                if (upperVal.equals("ECDH")) {
                    finding.setAlgorithm("ECDH");
                } else if (upperVal.contains("DH")) {
                    finding.setAlgorithm("DH");
                    finding.setVariant(literalValue);
                }
                break;
            case "MessageDigest":
                finding.setPurpose(CryptoFinding.Purpose.HASHING);
                finding.setAlgorithm(literalValue);
                break;
            case "SSLContext":
                finding.setPurpose(CryptoFinding.Purpose.PROTOCOL);
                finding.setAlgorithm("TLS");
                finding.setVariant(literalValue);
                break;
            case "KeyGenerator":
                finding.setPurpose(CryptoFinding.Purpose.KEY_GENERATION);
                if (upperVal.startsWith("AES")) {
                    finding.setAlgorithm("AES");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("ChaCha20")) {
                    finding.setAlgorithm("ChaCha20");
                    finding.setVariant(literalValue);
                } else if (upperVal.contains("HmacSHA")) {
                    finding.setAlgorithm("HMAC");
                    finding.setVariant(literalValue);
                    finding.setPurpose(CryptoFinding.Purpose.HASHING);
                }
                break;
            case "Mac":
                finding.setPurpose(CryptoFinding.Purpose.HASHING);
                if (upperVal.contains("Hmac")) {
                    finding.setAlgorithm("HMAC");
                    finding.setVariant(literalValue);
                }
                break;
        }
    }

    private void extractKeySize(MethodCallExpr n, CryptoFinding finding) {
        Optional<Node> parent = n.getParentNode();
        String varName = null;
        
        if (parent.isPresent() && parent.get() instanceof VariableDeclarator) {
            varName = ((VariableDeclarator) parent.get()).getNameAsString();
        } else if (parent.isPresent() && parent.get() instanceof AssignExpr) {
            varName = ((AssignExpr) parent.get()).getTarget().toString();
        }

        if (varName != null) {
            Optional<BlockStmt> block = n.findAncestor(BlockStmt.class);
            if (block.isPresent()) {
                String finalVarName = varName;
                block.get().findAll(MethodCallExpr.class).forEach(call -> {
                    if ("initialize".equals(call.getNameAsString()) 
                            && call.getScope().isPresent() 
                            && call.getScope().get().toString().equals(finalVarName)
                            && call.getArguments().isNonEmpty()
                            && call.getArgument(0).isIntegerLiteralExpr()) {
                        
                        int size = call.getArgument(0).asIntegerLiteralExpr().asInt();
                        finding.setKeySize(size);
                        finding.setVariant("RSA-" + size);
                    }
                });
            }
        }
    }
}