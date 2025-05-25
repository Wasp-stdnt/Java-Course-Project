package io.github.wasp_stdnt.passwordmanagerv2.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "passwords")
@Data
public class Password {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String service;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String credential;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String ciphertext;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String iv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
