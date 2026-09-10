package com.kavya.stealthpad.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notes")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String category;

    private long timestamp;

<<<<<<< Updated upstream
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")       // this connects the notes table with users table based on the user id...
    private User user;
=======
    private long updatedAt;

    @Column(nullable = false)
    private long version;

     @Column(nullable = false)
    private boolean deleted;

    @Column(name = "is_vault", nullable = false)
    private boolean isVault = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // this connects the notes table with users table based on the user id...
    private User user;

    @PrePersist
    public void onCreate() {
         long now = System.currentTimeMillis();

        timestamp = now;
        updatedAt = now;

        version = 1;
        deleted = false;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = System.currentTimeMillis();
        version += 1;

    }

>>>>>>> Stashed changes
}