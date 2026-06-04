package com.dende.eventos.repository;

import com.dende.eventos.model.Evento;
import com.dende.eventos.model.StatusEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByStatus(StatusEvento status);

    List<Evento> findByLocalContainingIgnoreCase(String local);

    List<Evento> findByDataInicioAfter(LocalDateTime data);

    @Query("SELECT e FROM Evento e WHERE e.dataInicio BETWEEN :inicio AND :fim")
    List<Evento> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT e FROM Evento e WHERE SIZE(e.participantes) < e.capacidadeMaxima AND e.status = :status")
    List<Evento> findEventosComVagasDisponiveis(@Param("status") StatusEvento status);

    boolean existsByNomeAndDataInicio(String nome, LocalDateTime dataInicio);
}
