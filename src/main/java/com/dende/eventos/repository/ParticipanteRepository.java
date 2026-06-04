package com.dende.eventos.repository;

import com.dende.eventos.model.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    List<Participante> findByEventoId(Long eventoId);

    Optional<Participante> findByEmailAndEventoId(String email, Long eventoId);

    boolean existsByCpfAndEventoId(String cpf, Long eventoId);

    boolean existsByEmailAndEventoId(String email, Long eventoId);
}
