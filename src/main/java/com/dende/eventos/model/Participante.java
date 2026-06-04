package com.dende.eventos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(name = "data_inscricao", nullable = false, updatable = false)
    private LocalDateTime dataInscricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    @PrePersist
    public void prePersist() {
        this.dataInscricao = LocalDateTime.now();
    }
}
