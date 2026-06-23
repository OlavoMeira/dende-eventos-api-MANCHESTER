package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.*;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.model.Organizador;
import br.com.softhouse.dende.repositories.Repositorio;
import br.com.softhouse.dende.utils.ResponseUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(path = "/organizadores")
public class OrganizadorController {

    private final Repositorio repositorio;

    public OrganizadorController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping
    public ResponseEntity<String> cadastroOrganizador(@RequestBody Organizador organizador) {
        // Validar campos obrigatórios
        if (organizador.getNome() == null || organizador.getNome().trim().isEmpty()) {
            return ResponseUtils.badRequest("Nome é obrigatório");
        }
        if (organizador.getDataNascimento() == null) {
            return ResponseUtils.badRequest("Data de nascimento é obrigatória");
        }
        if (organizador.getSexo() == null || organizador.getSexo().trim().isEmpty()) {
            return ResponseUtils.badRequest("Sexo é obrigatório");
        }
        if (organizador.getEmail() == null || organizador.getEmail().trim().isEmpty()) {
            return ResponseUtils.badRequest("Email é obrigatório");
        }
        if (organizador.getSenha() == null || organizador.getSenha().trim().isEmpty()) {
            return ResponseUtils.badRequest("Senha é obrigatória");
        }

        // Verificar email duplicado
        if (repositorio.existeOrganizadorComEmail(organizador.getEmail())) {
            return ResponseUtils.badRequest("Já existe um organizador cadastrado com este e-mail: " + organizador.getEmail());
        }

        if (repositorio.existeUsuarioComEmail(organizador.getEmail())) {
            return ResponseUtils.badRequest("Este e-mail já está cadastrado como usuário comum: " + organizador.getEmail());
        }

        repositorio.salvarOrganizador(organizador);

        return ResponseUtils.ok("Organizador " + organizador.getEmail() + " registrado com sucesso!");
    }

    @PutMapping(path = "/{organizadorId}")
    public ResponseEntity<String> alterarOrganizador(
            @PathVariable(parameter = "organizadorId") long organizadorId,
            @RequestBody Organizador organizador) {

        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            return ResponseUtils.badRequest("Organizador não encontrado com ID: " + organizadorId);
        }

        Organizador organizadorExistente = organizadorOpt.get();

        // Não permitir alteração de e-mail
        if (!organizadorExistente.getEmail().equals(organizador.getEmail())) {
            return ResponseUtils.badRequest("Não é permitido alterar o e-mail do organizador");
        }

        // Manter o ID
        organizador.setId(organizadorId);

        // Manter a senha se não foi fornecida
        if (organizador.getSenha() == null || organizador.getSenha().isEmpty()) {
            organizador.setSenha(organizadorExistente.getSenha());
        }

        // Manter o status ativo
        organizador.setAtivo(organizadorExistente.isAtivo());

        repositorio.salvarOrganizador(organizador);

        return ResponseUtils.ok("Organizador " + organizador.getEmail() + " alterado com sucesso!");
    }

    @GetMapping(path = "/{organizadorId}")
    public ResponseEntity<Object> visualizarPerfil(@PathVariable(parameter = "organizadorId") long organizadorId) {
        Optional<Organizador> organizadorOpt = repositorio.buscarOrganizadorPorId(organizadorId);

        if (!organizadorOpt.isPresent()) {
            // Retorna um Map com erro em vez de String
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Organizador não encontrado com ID: " + organizadorId);
            return ResponseUtils.ok(erro); // Agora retorna Object
        }

        Organizador organizador = organizadorOpt.get();

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("id", organizador.getId());
        perfil.put("nome", organizador.getNome());
        perfil.put("dataNascimento", organizador.getDataNascimento().toString());
        perfil.put("idadeCompleta", organizador.getIdadeCompleta());
        perfil.put("sexo", organizador.getSexo());
        perfil.put("email", organizador.getEmail());
        perfil.put("ativo", organizador.isAtivo());

        if (organizador.isEmpresa()) {
            Map<String, Object> empresa = new HashMap<>();
            empresa.put("cnpj", organizador.getCnpj());
            empresa.put("razaoSocial", organizador.getRazaoSocial());
            empresa.put("nomeFantasia", organizador.getNomeFantasia());
            perfil.put("empresa", empresa);
        }

        return ResponseUtils.ok(perfil);
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
            // Verificar se tem eventos ativos
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