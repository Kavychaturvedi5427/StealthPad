package com.kavya.stealthpad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kavya.stealthpad.Entity.Note;
import java.util.List;
import java.util.Optional;
import com.kavya.stealthpad.Entity.User;

import jakarta.transaction.Transactional;



public interface NotesRepository extends JpaRepository<Note, Long>{

    List<Note> findByUser(User user);

    Optional<Note> findByIdAndUser(Long id, User user); // purpose of this method is to ensure that only those notes will be returned that belong to the particular user...
    // if we simply user id and not user then it might return the notes of other user having similar note id...

    @Modifying  // without this spring will take it as select query...
    @Transactional
    @Query("delete from Note n where n.user = :user")
    void deleteByUser(@Param(value = "user") User user);
    
}
