package br.com.softhouse.dende.controllers;

import br.com.dende.softhouse.annotations.Controller;
import br.com.dende.softhouse.annotations.request.GetMapping;
import br.com.dende.softhouse.annotations.request.PostMapping;
import br.com.dende.softhouse.annotations.request.PutMapping;
import br.com.dende.softhouse.annotations.request.RequestBody;
import br.com.dende.softhouse.annotations.request.RequestMapping;
import br.com.dende.softhouse.annotations.request.PathVariable;
import br.com.dende.softhouse.process.route.ResponseEntity;
import br.com.softhouse.dende.model.Usuario;
import br.com.softhouse.dende.repositories.Repositorio;
import java.util.Collection;

@Controller
@RequestMapping(path = "/usuarios")
public class UsuarioController {

    private final Repositorio repositorio;

    public UsuarioController() {
        this.repositorio = Repositorio.getInstance();
    }

    @PostMapping
    public ResponseEntity<String> cadastroUsuario(@RequestBody Usuario usuario){
        // Agora o controlador realmente usa o repositório para salvar
        repositorio.salvarUsuario(usuario); 
        return ResponseEntity.ok("Usuario " + usuario.getNome() + " registrado com sucesso!");
    }

    @GetMapping
    public ResponseEntity<Collection<Usuario>> getUsuarios() {
        // Retorna a lista de usuários salvos no repositório
        return ResponseEntity.ok(repositorio.listarUsuarios());
    }

    @PutMapping(path = "/{usuarioId}")
    public ResponseEntity<String> alterarUsuario(@PathVariable(parameter = "usuarioId") long usuarioId, @RequestBody Usuario usuario) {
        // Atualiza os dados do usuário usando o ID vindo da URL
        usuario.setId(usuarioId);
        repositorio.salvarUsuario(usuario);
        return ResponseEntity.ok("Usuario " + usuario.getNome() + " (ID: " + usuarioId + ") alterado com sucesso!");
    }
}