package com.salao.cliente.repository;

import com.salao.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByEmail(String email);

    /** Busca o cliente vinculado a um usuário Keycloak específico. */
    Optional<Cliente> findByKeycloakUserId(String keycloakUserId);

    /**
     * Busca clientes inativos há mais tempo do que o limite informado
     * e que ainda não foram anonimizados — usada pelo job de expurgo LGPD.
     */
    List<Cliente> findByUltimaAtividadeBeforeAndAnonimizadoFalse(LocalDateTime limiteInatividade);
}
