package com.project.run_to_own.repositories;

import com.project.run_to_own.model.Friendship;
import com.project.run_to_own.model.FriendshipStatus;
import com.project.run_to_own.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterAndAddressee(User requester, User addressee);

    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    Optional<Friendship> findByRequesterAndAddresseeAndStatus(User requester, User addressee, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendshipsForUser(@Param("user") User user);
}