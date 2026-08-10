package com.booknook.backend.repository;

import com.booknook.backend.model.UserAccount;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserAccountRepository {

    public static final String COLLECTION = "users";

    private final Firestore firestore;

    public UserAccountRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public Optional<UserAccount> findByUid(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collection().document(uid).get().get();
        return snapshot.exists() ? Optional.ofNullable(snapshot.toObject(UserAccount.class)) : Optional.empty();
    }

    public UserAccount save(UserAccount account) throws ExecutionException, InterruptedException {
        collection().document(account.getUid()).set(account).get();
        return account;
    }

    public void deleteByUid(String uid) throws ExecutionException, InterruptedException {
        collection().document(uid).delete().get();
    }
}
