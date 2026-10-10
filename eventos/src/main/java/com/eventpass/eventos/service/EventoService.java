package com.eventpass.eventos.service;
import com.eventpass.eventos.dto.EventoRequest;
import com.eventpass.eventos.model.Evento;
import com.eventpass.eventos.repository.EventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service public class EventoService {
 private final EventoRepository repo;
 public EventoService(EventoRepository repo){this.repo=repo;}
 public List<Evento> listar(){return repo.findAll();}
 public Evento buscar(Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado"));}
 @Transactional public Evento crear(EventoRequest r){Evento e=new Evento();e.setNombre(r.nombre());e.setDescripcion(r.descripcion());e.setLugar(r.lugar());e.setFechaEvento(r.fechaEvento());e.setAforoTotal(r.aforoTotal());e.setAforoDisponible(r.aforoTotal());e.setEstado(r.estado()==null||r.estado().isBlank()?"PUBLICADO":r.estado().toUpperCase());return repo.save(e);}
 @Transactional public Evento actualizar(Long id,EventoRequest r){Evento e=buscar(id);int vendidos=e.getAforoTotal()-e.getAforoDisponible();if(r.aforoTotal()<vendidos)throw new ResponseStatusException(HttpStatus.CONFLICT,"El aforo total no puede ser menor que las entradas ya reservadas");e.setNombre(r.nombre());e.setDescripcion(r.descripcion());e.setLugar(r.lugar());e.setFechaEvento(r.fechaEvento());e.setAforoTotal(r.aforoTotal());e.setAforoDisponible(r.aforoTotal()-vendidos);if(r.estado()!=null&&!r.estado().isBlank())e.setEstado(r.estado().toUpperCase());return repo.save(e);}
 @Transactional public Evento reservar(Long id,int cantidad){if(cantidad<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La cantidad debe ser mayor que cero");int updated=repo.descontarAforo(id,cantidad);if(updated==0){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado");throw new ResponseStatusException(HttpStatus.CONFLICT,"Evento no publicado o aforo insuficiente");}return buscar(id);}
 @Transactional public void eliminar(Long id){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado");repo.deleteById(id);}
}
