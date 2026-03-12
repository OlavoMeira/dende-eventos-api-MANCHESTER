package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.dto.request.OrganizadorRequestDTO;
import br.com.softhouse.dende.dto.response.OrganizadorResponseDTO;
import br.com.softhouse.dende.mapper.OrganizadorMapper;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.Optional;

@Controller
@RequestMapping(path = "/organizadores")
public class OrganizadorController {

    private final Repositorio repositorio;

    public OrganizadorController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping
    public ResponseEntity<String> cadastroOrganizador(@RequestBody OrganizadorRequestDTO organizadorRequest) {
        if (organizadorRequest.getNome() == null || organizadorRequest.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome é obrigatório");
        }
        if (organizadorRequest.getDataNascimento() == null) {
            return ResponseUtils.badRequest("Data de nascimento é obrigatória");
        }
        if (organizadorRequest.getSexo() == null || organizadorRequest.getSexo().trim().isEmpty()) {
            return ResponseUtils.badRequest("Sexo é obrigatório");
        }
        if (organizadorRequest.getEmail() == null || organizadorRequest.getEmail().trim().isEmpty()) {
            return ResponseUtils.badRequest("Email é obrigatório");
        }
        if (organizadorRequest.getSenha() == null || organizadorRequest.getSenha().trim().isEmpty()) {
            return ResponseUtils.badRequest("Senha é obrigatória");
        }

        if (repositorio.existeOrganizadorComEmail(organizadorRequest.getEmail())) {
            return ResponseUtils.badRequest("Já existe um organizador cadastrado com este e-mail: " + organizadorRequest.getEmail());
        }

        if (repositorio.existeUsuarioComEmail(organizadorRequest.getEmail())) {
            return ResponseUtils.badRequest("Este e-mail já está cadastrado como usuário comum: " + organizadorRequest.getEmail());
        }

        Organizador organizador = OrganizadorMapper.toModel(organizadorRequest);
        repositorio.salvarOrganizador(organizador);

        return ResponseUtils.ok("Organizador " + organizador.getEmail() + " registrado com sucesso! ID: " + organizador.getId());
    }

    @PutMapping(path = "/{organizadorId}")
    public ResponseEntity<String> alterarOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @RequestBody OrganizadorRequestDTO organizadorRequest) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizadorExistente = organizadorOpt.get();

        if (!organizadorExistente.getEmail().equals(organizadorRequest.getEmail())) {
            return ResponseUtils.badRequest("Não é permitido alterar o e-mail do organizador");
        }

        OrganizadorMapper.updateModel(organizadorRequest, organizadorExistente);
        repositorio.salvarOrganizador(organizadorExistente);

        return ResponseUtils.ok("Organizador " + organizadorExistente.getEmail() + " alterado com sucesso!");
    }

    @GetMapping(path = "/{organizadorId}")
    public ResponseEntity<?> visualizarPerfil(@PathVariable(parameter = "organizadorId") long organizadorId) {
        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.notFound("Organizador não encontrado com ID: " + organizadorId);
        }

        OrganizadorResponseDTO response = OrganizadorMapper.toResponse(organizadorOpt.get());
        return ResponseUtils.ok(response);
    }

    @PatchMapping(path = "/{organizadorId}/{status}")
    public ResponseEntity<String> alterarStatus(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @PathVariable(parameter = "status") String status) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizador = organizadorOpt.get();

        if ("ativar".equalsIgnoreCase(status)) {
            organizador.setAtivo(true);
            repositorio.salvarOrganizador(organizador);
            return ResponseUtils.ok("Organizador reativado com sucesso!");
        } else if ("desativar".equalsIgnoreCase(status)) {
            if (repositorio.organizadorTemEventosAtivos(organizadorId)) {
                return ResponseUtils.badRequest("Não é possível desativar o organizador pois ele possui eventos ativos ou em execução");
            }

            organizador.setAtivo(false);
            repositorio.salvarOrganizador(organizador);
            return ResponseUtils.ok("Organizador desativado com sucesso!");
        } else {
            return ResponseUtils.badRequest("Status inválido. Use 'ativar' ou 'desativar'");
        }
    }
}