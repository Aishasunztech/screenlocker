package com.screenlocker.secure.room.security;

import android.content.Context;

import androidx.annotation.NonNull;

import com.screenlocker.secure.utils.SecuredSharedPref;

import java.io.IOException;
import java.security.SecureRandom;

import static com.screenlocker.secure.utils.AppConstants.DatabaseEncryptedSecret;
import static com.screenlocker.secure.utils.AppConstants.DatabaseUnencryptedSecret;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/3/2020
 */
public class DatabaseSecretProvider {

    @SuppressWarnings("unused")
    private static final String TAG = DatabaseSecretProvider.class.getSimpleName();

    private final Context context;
    private final SecuredSharedPref pref;

    public DatabaseSecretProvider(@NonNull Context context) {
        this.context = context.getApplicationContext();
        pref = SecuredSharedPref.getInstance(context);
    }

    public DatabaseSecret getOrCreateDatabaseSecret() {
        String unencryptedSecret = pref.getStringPref(DatabaseUnencryptedSecret);
//        String unencryptedSecret = TextSecurePreferences.getDatabaseUnencryptedSecret(context);
        String encryptedSecret = pref.getStringPref(DatabaseEncryptedSecret);
//        String encryptedSecret   = TextSecurePreferences.getDatabaseEncryptedSecret(context);

        if (unencryptedSecret != null)
            return getUnencryptedDatabaseSecret(context, unencryptedSecret);
        else if (encryptedSecret != null) return getEncryptedDatabaseSecret(encryptedSecret);
        else return createAndStoreDatabaseSecret(context);
    }

    private DatabaseSecret getUnencryptedDatabaseSecret(@NonNull Context context, @NonNull String unencryptedSecret) {
        try {
            DatabaseSecret databaseSecret = new DatabaseSecret(unencryptedSecret);

            KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());

            pref.saveStringPref(DatabaseEncryptedSecret, encryptedSecret.serialize());
//            TextSecurePreferences.setDatabaseEncryptedSecret(context, encryptedSecret.serialize());
            pref.saveStringPref(DatabaseUnencryptedSecret, null);

            return databaseSecret;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private DatabaseSecret getEncryptedDatabaseSecret(@NonNull String serializedEncryptedSecret) {
        KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.SealedData.fromString(serializedEncryptedSecret);
        return new DatabaseSecret(KeyStoreHelper.unseal(encryptedSecret));
    }

    private DatabaseSecret createAndStoreDatabaseSecret(@NonNull Context context) {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[32];
        random.nextBytes(secret);

        DatabaseSecret databaseSecret = new DatabaseSecret(secret);

        KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());
        pref.saveStringPref(DatabaseEncryptedSecret, encryptedSecret.serialize());

        return databaseSecret;
    }
}
