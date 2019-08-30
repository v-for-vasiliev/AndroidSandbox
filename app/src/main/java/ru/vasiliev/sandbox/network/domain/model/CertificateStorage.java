package ru.vasiliev.sandbox.network.domain.model;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import timber.log.Timber;

public final class CertificateStorage {

    private KeyStore mKeyStore;

    public CertificateStorage() {
        init();
    }

    private void init() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
        } catch (final IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            Timber.e("", e);
        }
    }

    public void saveCertificate(String alias, Certificate certificate) {
        try {
            mKeyStore.setCertificateEntry(alias, certificate);
        } catch (KeyStoreException e) {
            Timber.e("", e);
        }
    }

    public boolean hasCertificate(String alias) {
        try {
            return mKeyStore.containsAlias(alias);
        } catch (final Exception e) {
            Timber.e("", e);
        }
        return false;
    }

    public Certificate loadCertificate(String alias) {
        try {
            return mKeyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            Timber.e("", e);
        }
        return null;
    }

    public String calculateSHA256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(bytes);
            return String.format("%064x", new java.math.BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            Timber.e("", e);
        }
        return null;
    }
}